#![cfg_attr(not(feature = "std"), no_std)]

use liquid::storage;
use liquid_lang as liquid;
use liquid_lang::InOut;
use liquid_prelude::{string::String, vec::Vec};

#[derive(InOut)]
pub struct KVField {
    key: String,
    value: String,
}
#[derive(InOut)]
pub struct Entry {
    fileds: Vec<KVField>,
}

#[liquid::interface(name = auto)]
mod kv_table {
    use super::*;

    extern "liquid" {
        fn createTable(
            &mut self,
            table_name: String,
            key: String,
            value_fields: String,
        ) -> i256;
        fn get(&self, table_name: String, key: String) -> (bool, Entry);
        fn set(&mut self, table_name: String, key: String, entry: Entry) -> i256;
    }
}

#[liquid::contract]
mod kv_table_test {
    use super::{kv_table::*, *};

    #[liquid(storage)]
    struct KvTableTest {
        table: storage::Value<KvTable>,
    }

    #[liquid(event)]
    struct SetResult {
        count: i256,
    }

    #[liquid(methods)]
    impl KvTableTest {
        pub fn new(&mut self) {
            self.table
                .initialize(KvTable::at("/sys/kv_storage".parse().unwrap()));
            self.table.createTable(
                String::from("t_kv_test"),
                String::from("id"),
                [String::from("item_price"), String::from("item_name")].join(","),
            );
        }

        pub fn get(&self, id: String) -> (bool, String, String) {
            if let Some((ok, entry)) = (*self.table).get(String::from("t_kv_test"), id) {
                return (
                    ok,
                    entry.fileds[0].value.clone(),
                    entry.fileds[1].value.clone(),
                );
            }
            return (false, Default::default(), Default::default());
        }

        pub fn set(&mut self, id: String, item_price: String, item_name: String) -> i256 {
            let kv1 = KVField {
                key: String::from("item_price"),
                value: item_price,
            };
            let kv2 = KVField {
                key: String::from("item_name"),
                value: item_name,
            };
            let mut kv_fields = Vec::new();
            kv_fields.push(kv1);
            kv_fields.push(kv2);
            let entry = Entry { fileds: kv_fields };
            let count = (*self.table)
                .set(String::from("t_kv_test"), id, entry)
                .unwrap();

            self.env().emit(SetResult {
                count: count.clone(),
            });
            count
        }
    }
}
