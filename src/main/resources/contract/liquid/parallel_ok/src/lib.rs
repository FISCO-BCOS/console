#![cfg_attr(not(feature = "std"), no_std)]

use liquid::storage;
use liquid_lang as liquid;

#[liquid::contract]
mod parallel_ok {
    use super::*;

    type Balance = u128;

    #[liquid(storage)]
    struct ParallelOk {
        balances: storage::Mapping<String, Balance>,
    }

    #[liquid(methods)]
    impl ParallelOk {
        pub fn new(&mut self) {
            self.balances.initialize();
        }

        pub fn balance_of(&self, name: String) -> Balance {
            *self.balances.get(&name).unwrap_or(&0)
        }

        pub fn set(&mut self, name: String, num: Balance) {
            self.balances.insert(name, num);
        }

        pub fn transfer(&mut self, from: String, to: String, value: Balance) -> bool {
            let from_balance = self.balance_of(from.clone());
            if from_balance < value {
                return false;
            }

            self.balances.insert(from.clone(), from_balance - value);
            let to_balance = self.balance_of(to.clone());
            self.balances.insert(to.clone(), to_balance + value);
            true
        }
    }
}
