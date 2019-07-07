(ns clojucture.local.china.local_cn
  (:require
    [clojucture.asset :as asset]
    [clojucture.account :as acc]
    [clojucture.spv :as d]
    [clojucture.bond :as b]
    [clojucture.expense :as exp]
    [clojucture.trigger :as trigger]
    [clojucture.pool :as p]
    [clojucture.util :as u]
    [clojucture.reader.base :as rb]
    [java-time :as jt]
    [clojure.core.match :as m])
  (:use [clojure.core.match.regex])
  (:import java.util.Arrays)
  )




(defn setup-accounts [d u]
  "d -> deal map, u -> deal update"
  (loop [accs [] accs-to-add (get-in d [:snapshot u :账户])]
    (if (nil? accs-to-add)
      accs
      (recur
        (conj accs
              (m/match (first accs-to-add)
                       {:名称 acc-name} (acc/->account acc-name nil 0 [])
                       :else nil))
        (next accs-to-add)))
    ))
(defn setup-asset-mortgage [a]
  (m/match a
           {:初始面额 original-bal :当前面额 current-bal :年利率 annual-rate :摊销 amort-type :初始日 start-date
            :支付日期 pay-dates :初始期限 original-term :当前期限 remain-term}
           (asset/->mortgage
             {:start-date  (rb/parsing-dates start-date) :balance original-bal :periodicity (jt/months 1) :term original-term
              :period-rate (/ annual-rate 12)}
             nil current-bal (/ annual-rate 12) remain-term nil)


           :else nil
           )

  )

(defn setup-assets [d u]
  (let [assets (get-in d [:snapshot u :资产池 :资产清单])
        coll-type (get-in d [:snapshot u :资产池 :类型])]
    (m/match coll-type
             :住房按揭 (map setup-asset-mortgage assets)
             :else nil)
    ))

(defn setup-pool [d u]
  (let [assets (setup-assets d u)
        cut-off-date (-> (get-in d [:snapshot u :资产池 :封包日]) (rb/parsing-dates))
        ]
    (p/->pool assets cut-off-date)

    ))


(defn setup-dates [d]
  (let [cut-off-date (jt/local-date (get-in d [:日期 :初始起算日]))
        stated-maturity-date (get-in d [:日期 :法定到期日])]
    {
     :cut-off-date    cut-off-date
     :stated-maturity stated-maturity-date
     :pay-dates       (rb/parsing-dates (str (get-in d [:日期 :支付日]) "," stated-maturity-date))
     :int-dates       (rb/parsing-dates (str (get-in d [:日期 :计息日]) "," stated-maturity-date))
     :calc-dates      (rb/parsing-dates (str (get-in d [:日期 :计算日]) "," stated-maturity-date))
     :dist-dates      (rb/parsing-dates (str (get-in d [:日期 :信托分配日]) "," stated-maturity-date))
     }
    )
  )


(defn setup-expense [x]
  (m/match x
           {:name n :last-paid-date pd :year-rate r :arrears ars}
           (exp/->pct-expense-by-amount {:name n :pct r :day-count :ACT_365} nil pd ars)
           {:name n :balance v :last-paid-date pd}
           (exp/->amount-expense {:name n} nil pd v)
           {:name n :last-paid-date pd :base-rate r :arrears ars}
           (exp/->pct-expense-by-rate {:name n :pct r} nil pd ars)
           :else :not-match-expense
           )
  )


(defn setup-expenses [d u]
  (let [exps (get-in d [:snapshot u :费用])]
    (map setup-expense exps)))

(defn setup-trigger [x]
  (m/match x
           {:name n :cond [:资产池违约率 :大于 th]}
           (trigger/->trigger {:name n :watch :pool-cumulative-default-rate} > th)


           :else :not-match-trigger)

  )

(defn setup-triggers [d u]
  (let [trgs (get-in d [:snapshot u :风险事件])]
    (map setup-trigger trgs)
    )
  )

;{:简称 "A-1" :初始面额 2550000000 :当前余额 2550000000 :预期到期日 "2019-02-26" :法定到期日 "2047-4-26"}
; [ info balance rate stmts last-payment-date interest-arrears principal-loss ]
(defn setup-bond [ bnd ]
  (m/match bnd
           {:简称 label :初始面额  orig-bal :当前余额 cur-bal :执行利率 cur-rate :预期到期日 expected-date :法定到期日 stated-maturity-date}
           (b/->sequence-bond {:day-count :ACT_365} cur-bal cur-rate [] nil 0 0)
           {:简称 label :初始面额  orig-bal :当前余额 cur-bal :预期到期日 expected-date :法定到期日 stated-maturity-date}
           (b/->equity-bond {:day-count :ACT_365} cur-bal [] nil )
           :else :not-match-bond-map
           )

  )


(defn setup-bonds [d u]
  (let [ bnds (get-in d [:snapshot u :债券])]
    (map setup-bond bnds)))


(comment




  (defrecord china-bank-deal [deal-info opt status update-date]
    d/Deal
    (run-assets [x assump]
      (let [
            ]
        )
      )
    (run-triggers [x]

      )
    (run-bonds [x assump]

      )
    )



  (defn build-deal [deal-type deal-structure]
    (let [
          update (get-in deal-structure [:meta :update-date])
          accounts (setup-accounts deal-structure)
          dates (setup-dates deal-structure)
          pool (setup-pool deal-structure)
          bond (setup-bond deal-structure)
          triggers (setup-triggers deal-structure)
          expense (setup-expense deal-structure)
          ]
      (case deal-type
        ;:bank-deal (china-bank-deal. nil nil nil )

        )
      )
    )
  )