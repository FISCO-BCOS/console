#![cfg_attr(not(feature = "std"), no_std)]

use liquid::storage;
use liquid_lang as liquid;

#[liquid::contract]
mod asset {
    use super::{*};

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
        asset_table: storage::Mapping<String, u128>,
    }

    #[liquid(methods)]
    impl Asset {
        pub fn new(&mut self) {
            self.asset_table.initialize();
        }

        pub fn select(&mut self, account: String) -> (bool, u128) {
            if self.asset_table.contains_key(&account) {
               return (true, self.asset_table[&account]) 
            }
            return (false, 0)
        }

        pub fn register(&mut self, account: String, asset_value: u128) -> i16 {
            let ret_code: i16;
            let (ok, _) = self.select(account.clone());
            if ok == false {
                self.asset_table.insert(
                    account.clone(), asset_value
                );
                ret_code = 0;
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


            self.asset_table.insert(
                from.clone(),from_value - value.clone()
            );

            self.asset_table.insert(
                to.clone(),to_value.clone() + value.clone()
            );

            self.env().emit(TransferEvent {
                ret_code,
                from,
                to,
                value,
            });
            return ret_code;
        }
    }
}
