(ns clojucture.account
  (:require [clojucture.type :as t]
            [clojucture.core :as c]
            [java-time :as jt]
            )
  (:import
    [tech.tablesaw.api Table DoubleColumn DateColumn StringColumn BooleanColumn]
    [java.time LocalDate])
  )




(defrecord account [ name type ^Double balance stmts ]
  t/Account
  (withdraw [ x d to amount ]
    (.deposit x d to (- amount))
    )
  (try-withdraw [ x d to amount ]
    (let [ max-to-draw (min amount balance)]
    (.deposit x d to (- max-to-draw)))
    )

  (deposit [ x  d from amount ]
    (let [ new-statment (c/->stmt d from :this amount nil)
           new-balance (+ balance amount)]
      (->account name type new-balance (conj stmts new-statment))
      )
    )

  (last-txn [ x ]
    (last stmts)
    )
  )

(defn transfer-fund [ from-acc to-acc ^LocalDate d ^Double amount ]
  (if (>= (:balance from-acc) amount)
    (dosync
      [ (.withdraw from-acc d to-acc amount) (.deposit to-acc d from-acc amount)]
      )
    nil)
  )