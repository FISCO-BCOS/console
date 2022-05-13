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

#[derive(InOut)]
pub struct Entry {
    key: String,
    fields: Vec<String>,
}

#[derive(InOut)]
pub struct UpdateField {
    index: u32,
    value: String,
}

#[derive(InOut)]
pub enum ConditionOP {
    GT(u8),
    GE(u8),
    LT(u8),
    LE(u8),
}

#[derive(InOut)]
pub struct Condition {
    op: ConditionOP,
    value: String,
}

#[derive(InOut)]
pub struct Limit {
    offset: u32,
    count: u32,
}

#[liquid::interface(name = auto)]
mod table_manager {
    use super::*;

    extern "liquid" {
        fn createTable(&mut self, path: String, table_info: TableInfo) -> i32;
    }
}
#[liquid::interface(name = auto)]
mod table {
    use super::*;

    extern "liquid" {
        fn select(&self, key: String) -> Entry;
        fn insert(&mut self, entry: Entry) -> i32;
        fn update(&mut self, key: String, update_fields: Vec<UpdateField>) -> i32;
        fn remove(&mut self, key: String) -> i32;
        fn desc(&self) -> TableInfo;
    }
}

#[liquid::contract]
mod table_test {
    use super::{table::*, table_manager::*, *};

    #[liquid(event)]
    struct InsertResult {
        count: i32,
    }

    #[liquid(event)]
    struct UpdateResult {
        count: i32,
    }
    #[liquid(event)]
    struct RemoveResult {
        count: i32,
    }

    #[liquid(storage)]
    struct TableTest {
        table: storage::Value<Table>,
        tm: storage::Value<TableManager>,
        table_name: storage::Value<String>,
    }

    #[liquid(methods)]
    impl TableTest {
        pub fn new(&mut self) {
            self.table_name.initialize(String::from("t_test"));
            self.tm
                .initialize(TableManager::at("/sys/table_manager".parse().unwrap()));

            let mut column_names = Vec::new();
            column_names.push(String::from("name"));
            column_names.push(String::from("age"));
            let ti = TableInfo {
                key_column: String::from("id"),
                value_columns: column_names,
            };

            let result = self.tm.createTable(self.table_name.clone(), ti);
            require(result.unwrap() == 0, "create table failed");
            self.table
                .initialize(Table::at("/tables/t_test".parse().unwrap()));
        }

        pub fn select(&mut self, id: String) -> (String, String) {
            let entry = self.table.select(id).unwrap();

            if entry.fields.len() < 1 {
                return (Default::default(), Default::default());
            }

            return (entry.fields[0].clone(), entry.fields[1].clone());
        }

        pub fn insert(&mut self, id: String, name: String, age: String) -> i32 {
            let mut values = Vec::new();
            values.push(name);
            values.push(age);

            let entry = Entry {
                key: id,
                fields: values,
            };
            let result = self.table.insert(entry).unwrap();
            self.env().emit(InsertResult {
                count: result.clone(),
            });
            return result;
        }

        pub fn update(&mut self, id: String, name: String, age: String) -> i32 {
            let mut update_fields = Vec::new();
            update_fields.push(UpdateField {
                index: 0,
                value: name,
            });

            update_fields.push(UpdateField {
                index: 1,
                value: age,
            });

            let result = self.table.update(id, update_fields).unwrap();
            self.env().emit(UpdateResult {
                count: result.clone(),
            });
            return result;
        }

        pub fn remove(&mut self, id: String) -> i32 {
            let result = self.table.remove(id).unwrap();
            self.env().emit(RemoveResult {
                count: result.clone(),
            });
            return result;
        }
    }
}
