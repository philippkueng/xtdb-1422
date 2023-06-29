(ns core
  (:require [xtdb.api :as xt]))

(def node
  (xt/start-node {:xtdb/index-store  {}
                  :xtdb/tx-log {}
                  :xtdb/document-store
                  {:xtdb/module "xtdb.azure.blobs/->document-store"
                   :sas-token "token"
                   :storage-account "account"
                   :container "container"}}))
;; => #'core/node

;; submit an entity with a keyword as an id and count entities -> all works
(xt/submit-tx node [[::xt/put {:xt/id :test-entity
                               :name "test entity with keyword"}]])
;; => #:xtdb.api{:tx-id 0, :tx-time #inst "2023-06-29T08:16:27.152-00:00"}

(count (xt/q (xt/db node)
             '{:find [?e]
               :where [[?e :name]]}))
;; => 1

;; submit an entity with a UUID as id and count entities -> they can't be queried and subsequent inserts don't show up.
(xt/submit-tx node [[::xt/put {:xt/id (java.util.UUID/randomUUID)
                               :name "test entity with UUID"}]])
;; => #:xtdb.api{:tx-id 1, :tx-time #inst "2023-06-29T08:16:40.632-00:00"}

(count (xt/q (xt/db node)
             '{:find [?e]
               :where [[?e :name]]}))
;; => 1

(xt/q (xt/db node)
      '{:find [?e ?name]
        :where [[?e :name ?name]]})
;; => #{[:test-entity "test entity with keyword"]}

(xt/status node)
;; => {:xtdb.version/version "1.23.2", :xtdb.version/revision "b35a2ca9c87075a8d471e673e84ee4b5eb58f520", :xtdb.index/index-version 22, :xtdb.tx-log/consumer-state nil, :xtdb.kv/kv-store "xtdb.mem_kv.MemKv", :xtdb.kv/estimate-num-keys 29, :xtdb.kv/size nil, :ingester-failed? false}

(xt/sync node (java.time.Duration/ofMinutes 1))
;; => #inst "2023-06-29T08:16:40.632-00:00"

(comment
  (.close node)
  )
