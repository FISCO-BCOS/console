#![cfg_attr(not(feature = "std"), no_std)]

use liquid::storage;
use liquid_lang as liquid;
use liquid_prelude::string::ToString;

#[liquid::interface(name = auto)]
mod table_manager {

    extern "liquid" {
        fn createKVTable(
            &mut self,
            table_name: String,
            key: String,
            value_fields: String,
        ) -> i32;
    }
}
#[liquid::interface(name = auto)]
mod kv_table {

    extern "liquid" {
        fn get(&self, key: String) -> (bool, String);
        fn set(&mut self, key: String, value: String) -> i32;
    }
}

#[liquid::contract]
mod asset {
    use super::{kv_table::*, table_manager::*, *};

    #[liquid(event)]
    struct RegisterEvent {
        ret_code: i16,
        #[liquid(indexed)]
        account: String,
        #[liquid(indexed)]
        asset_value: u128,
    }

    #[liquid(event)]
    struct TransferEvent {
        ret_code: i16,
        #[liquid(indexed)]
        from: String,
        #[liquid(indexed)]
        to: String,
        value: u128,
    }

    #[liquid(storage)]
    struct Asset {
        table: storage::Value<KvTable>,
        tm: storage::Value<TableManager>,
        table_name: storage::Value<String>,
    }

    #[liquid(methods)]
    impl Asset {
        pub fn new(&mut self) {
            self.table_name.initialize(String::from("t_asset"));
            self.tm
                .initialize(TableManager::at("/sys/table_manager".parse().unwrap()));
            self.tm.createKVTable(
                self.table_name.clone(),
                String::from("account"),
                String::from("asset_value"),
            );
            self.table
                .initialize(KvTable::at("/tables/t_asset".parse().unwrap()));
        }

        pub fn select(&self, account: String) -> (bool, u128) {
            if let Some((result, value)) = (*self.table).get(account) {
                if value.len() == 0 {
                    return (false, Default::default());
                }
                return (result, u128::from_str_radix(&value, 10).ok().unwrap());
            }
            return (false, Default::default());
        }

        pub fn register(&mut self, account: String, asset_value: u128) -> i16 {
            let ret_code: i16;
            let (ok, _) = self.select(account.clone());
            if ok == false {
                let result = (*self.table)
                    .set(account.clone(), asset_value.to_string())
                    .unwrap();

                if result == 1.into() {
                    ret_code = 0;
                } else {
                    ret_code = -2;
                }
            } else {
                ret_code = -1;
            }
            let ret = ret_code.clone();
            self.env().emit(RegisterEvent {
                ret_code,
                account,
                asset_value,
            });
            return ret;
        }

        pub fn transfer(&mut self, from: String, to: String, value: u128) -> i16 {
            let mut ret_code: i16 = 0;
            let (ok, from_value) = self.select(from.clone());
            if ok != true.into() {
                ret_code = -1;
                self.env().emit(TransferEvent {
                    ret_code,
                    from,
                    to,
                    value,
                });
                return ret_code;
            }

            let (ret, to_value) = self.select(to.clone());
            if ret != true {
                ret_code = -2;
                self.env().emit(TransferEvent {
                    ret_code,
                    from,
                    to,
                    value,
                });
                return ret_code;
            }

            if from_value < value.clone() {
                ret_code = -3;
                self.env().emit(TransferEvent {
                    ret_code,
                    from,
                    to,
                    value,
                });
                return ret_code;
            }

            if to_value.clone() + value.clone() < to_value.clone() {
                ret_code = -4;
                self.env().emit(TransferEvent {
                    ret_code,
                    from,
                    to,
                    value,
                });
                return ret_code;
            }

            let from_u = self.update(from.clone(), from_value - value.clone());
            if from_u != 1 {
                ret_code = -5;
                self.env().emit(TransferEvent {
                    ret_code,
                    from,
                    to,
                    value,
                });
                return ret_code;
            }

            let to_u = self.update(to.clone(), to_value.clone() + value.clone());
            if to_u != 1 {
                ret_code = -6;
            }
            self.env().emit(TransferEvent {
                ret_code,
                from,
                to,
                value,
            });
            return ret_code;
        }

        pub fn update(&mut self, account: String, value: u128) -> i16 {
            let r = (*self.table).set(account, value.to_string()).unwrap();
            if r == 1.into() {
                return 1;
            }
            return -1;
        }
    }
}
