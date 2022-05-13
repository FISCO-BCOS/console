#![cfg_attr(not(feature = "std"), no_std)]

use liquid::storage;
use liquid_lang as liquid;
use liquid_lang::InOut;
use liquid_prelude::{string::String, vec::Vec};

#[derive(InOut)]
pub struct TableInfo {
    key_column: String,
    value_columns: Vec<String>,
}

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
    use super::*;

    extern "liquid" {
        fn desc(&self) -> TableInfo;
        fn get(&self, key: String) -> (bool, String);
        fn set(&mut self, key: String, value: String) -> i32;
    }
}

#[liquid::contract]
mod kv_table_test {
    use super::{kv_table::*, table_manager::*, *};

    #[liquid(storage)]
    struct KvTableTest {
        table: storage::Value<KvTable>,
        tm: storage::Value<TableManager>,
        table_name: storage::Value<String>,
    }

    #[liquid(event)]
    struct SetEvent {
        count: i32,
    }

    #[liquid(methods)]
    impl KvTableTest {
        pub fn new(&mut self) {
            self.table_name.initialize(String::from("t_kv_test"));
            self.tm
                .initialize(TableManager::at("/sys/table_manager".parse().unwrap()));
            let result = self.tm.createKVTable(
                self.table_name.clone(),
                String::from("id"),
                String::from("item_name"),
            );
            require(result.unwrap() == 0, "create table failed");
            self.table
                .initialize(KvTable::at("/tables/t_kv_test".parse().unwrap()));
        }

        pub fn get(&self, id: String) -> (bool, String) {
            if let Some((ok, value)) = (*self.table).get(id) {
                return (ok, value);
            }
            return (false, Default::default());
        }

        pub fn set(&mut self, id: String, item_name: String) -> i32 {
            let count = (*self.table).set(id, item_name).unwrap();

            self.env().emit(SetEvent {
                count: count.clone(),
            });
            count
        }

        pub fn desc(&self) -> (String, String) {
            let ti = self.table.desc().unwrap();
            return (ti.key_column, ti.value_columns.get(0).unwrap().clone());
        }
    }
}
