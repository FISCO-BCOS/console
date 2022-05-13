use std::{collections::HashMap, env};

fn main() -> Result<(), std::io::Error> {
    let mut abi = HashMap::new();

    let contract_abi = <contract::__LIQUID_ABI_GEN as liquid_lang::GenerateAbi>::generate_abi();

    let mut local_abi =
        Vec::with_capacity(contract_abi.event_abis.len() + contract_abi.fn_abis.len() + 1);
    local_abi.extend(
        contract_abi
            .event_abis
            .iter()
            .map(|event_abi| liquid_lang::AbiKind::Event(event_abi.clone())),
    );
    local_abi.push(liquid_lang::AbiKind::Constructor(
        contract_abi.constructor_abi,
    ));
    local_abi.extend(
        contract_abi
            .fn_abis
            .iter()
            .map(|fn_abi| liquid_lang::AbiKind::ExternalFn(fn_abi.clone())),
    );
    abi.insert(String::from("$local"), local_abi);

    for (iface_name, fn_abis) in contract_abi.iface_abis {
        let fn_abis = fn_abis
            .iter()
            .map(|fn_abi| liquid_lang::AbiKind::ExternalFn(fn_abi.clone()))
            .collect::<Vec<liquid_lang::AbiKind>>();
        abi.insert(iface_name, fn_abis);
    }

    let target_dir = env::var("CARGO_TARGET_DIR").unwrap_or("target".into());
    std::fs::create_dir(&target_dir).ok();
    std::fs::write("kv_table_test.abi", serde_json::to_string(&abi).unwrap())?;
    Ok(())
}
