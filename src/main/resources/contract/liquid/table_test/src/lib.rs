#![cfg_attr(not(feature = "std"), no_std)]

use liquid::storage;
use liquid_lang as liquid;
use liquid_lang::InOut;
use liquid_prelude::{
    string::{String, ToString},
    vec::Vec,
};

#[derive(InOut)]
pub struct TableInfo {
    key_order: u8,
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
    column_name: String,
    value: String,
}

#[derive(InOut)]
pub struct Condition {
    op: u8,
    field: String,
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
        fn select(&self, key: Vec<Condition>, limit: Limit) -> Vec<Entry>;
        fn insert(&mut self, entry: Entry) -> i32;
        fn update(
            &mut self,
            key: Vec<Condition>,
            limit: Limit,
            update_fields: Vec<UpdateField>,
        ) -> i32;
        fn remove(&mut self, key: Vec<Condition>, limit: Limit) -> i32;
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
            self.table_name.initialize(String::from("t_testV320"));
            self.tm
                .initialize(TableManager::at("/sys/table_manager".parse().unwrap()));

            let mut column_names: Vec<String> = Vec::new();
            column_names.push(String::from("name"));
            column_names.push(String::from("age"));
            column_names.push(String::from("status"));
            let ti = TableInfo {
                key_order: 1,
                key_column: String::from("id"),
                value_columns: column_names,
            };

            self.tm.createTable(self.table_name.clone(), ti);
            self.table
                .initialize(Table::at("/tables/t_testV320".parse().unwrap()));
        }

        pub fn select(&self, id_low: i64, id_high: i64) -> Vec<Entry> {
            let limit = Limit {
                offset: 0,
                count: 500,
            };
            let mut conditions: Vec<Condition> = Vec::new();
            conditions.push(Condition {
                op: 0,
                field: String::from("id"),
                value: id_low.to_string(),
            });

            conditions.push(Condition {
                op: 3,
                field: String::from("id"),
                value: id_high.to_string(),
            });

            let entries = self.table.select(conditions, limit).unwrap();
            return entries;
        }

        pub fn insert(&mut self, id: i64, name: String, age: String) -> i32 {
            let mut values = Vec::new();
            values.push(name);
            values.push(age);
            values.push(String::from("init"));

            let entry = Entry {
                key: id.to_string(),
                fields: values,
            };
            let result = self.table.insert(entry).unwrap();
            self.env().emit(InsertResult {
                count: result.clone(),
            });
            return result;
        }

        pub fn update(&mut self, id_low: i64, id_high: i64) -> i32 {
            let mut update_fields = Vec::new();
            update_fields.push(UpdateField {
                column_name: String::from("status"),
                value: String::from("updated"),
            });

            let limit = Limit {
                offset: 0,
                count: 500,
            };
            let mut conditions: Vec<Condition> = Vec::new();
            conditions.push(Condition {
                op: 0,
                field: String::from("id"),
                value: id_low.to_string(),
            });

            conditions.push(Condition {
                op: 3,
                field: String::from("id"),
                value: id_high.to_string(),
            });

            let result = self.table.update(conditions, limit, update_fields).unwrap();
            self.env().emit(UpdateResult {
                count: result.clone(),
            });
            return result;
        }

        pub fn remove(&mut self, id_low: i64, id_high: i64) -> i32 {
            let limit = Limit {
                offset: 0,
                count: 500,
            };
            let mut conditions: Vec<Condition> = Vec::new();
            conditions.push(Condition {
                op: 0,
                field: String::from("id"),
                value: id_low.to_string(),
            });

            conditions.push(Condition {
                op: 3,
                field: String::from("id"),
                value: id_high.to_string(),
            });
            let result = self.table.remove(conditions, limit).unwrap();
            self.env().emit(RemoveResult {
                count: result.clone(),
            });
            return result;
        }
    }
}
