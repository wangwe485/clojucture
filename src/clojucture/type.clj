(ns clojucture.type
  (:require [clojucture.util :as u])
  (:import
    [tech.tablesaw.api Table DoubleColumn DateColumn StringColumn BooleanColumn]
    [tech.tablesaw.columns AbstractColumn]
    ;(clojucture.type Cashflow)

    ;(clojucture.type IndexCurve)
    (org.apache.commons.math3.util DoubleArray))

	)




(defprotocol Asset
	(project-cashflow [ x ] [ x assump ]  "project cashflow with/out assumption")
	)

(defprotocol Liability
  (cal-due-amount [ x d ] [ x d base ] )
  (receive [x d acc] [x d base acc] )
  )



(comment
(defprotocol Pool
	(project-cashflow [ x ] )
	(collect-cashflow [ x ] )
	)
)

(defprotocol Bond
  (cal-due-principal [ x d ] )
  (cal-due-interest [ x d ] )
  (amortize [ x d amt])
  (cal-next-rate [ x d assump ])
  (receive-payments [ x d principal interest ])
	;(load [ s ])
	)

(defprotocol Deal
	(run-assets [x assump] )
	(run-bonds [x assump] )
	)

(defprotocol IndexCurve
  (apply-to [ x float-info dates  ]))

(defprotocol pTable
	(merge1 [x t]))

(defprotocol Account
	(withdraw [ x d to amount ])
  (try-withdraw [ x d to amount ])
	(deposit [ x d from amount ])
  (last-txn [ x ])
	)

(defprotocol TableColumn
  (to-column [x] [x name])
  )


(extend-protocol TableColumn
  (class (double-array 0))
    (to-column [x] (DoubleColumn/create "EMPTY" x))
  )

