(ns english.core
  (:require [korma core db ]
            [ring.util.response :as response]
            [clojure.data.json :as json]
            [clj-http.client :as client]
            (compojure handler route))
  (:use [compojure.core :only (GET PUT POST defroutes)]
        [ring.adapter.jetty :only (run-jetty)]))

(def mofunUrl "http://www.mofunenglish.com/index.php?act=ajax&mdl=course&func=get_section_cont&section_id=")
(def OK_ID 75413)

(defn foo
  []
  (:body (client/get (str mofunUrl OK_ID))))

(defn readMofun
  []
  (let [status ((json/read-str (slurp "resources/data.json")) "status")]
    (if (= status "ok")
      "ok")))

(declare app* request )
(def waitUser (ref ""))
(defn pairTo [user]
  (if (or (empty? @waitUser)
          (= @waitUser user))
    (do
      (ref-set waitUser user)
      {:status 200
       :body (json/write-str {"status" "wait"})})
    (let [res {:status 200
               :body (json/write-str {"status" "paired"
                      "pairedUser" @waitUser})}]
      (ref-set waitUser "")
      res)))

(defn cancel [user]
  (if (= @waitUser user)
    (do
      (ref-set waitUser "")
      {:status 200
       :body "cancelSucceed"})
    {:status 200
     :body "cancelFailed"}))

(defroutes
  app*
  (GET "/pair" [user type]
       (dosync
         (cond
           (= type "pair") (pairTo user)
           (= type "cancel") (cancel user))))
  (compojure.route/not-found "Sorry,there's nothing here!"))

(def app (compojure.handler/api app*))

(declare server)

(try
  (.stop @server)
  (catch Exception e))

(def server (ref 'a))

(defn run-server []
  (dosync (ref-set server (run-jetty #'app
                                     {:host "127.0.0.1"
                                      :port 8085 :join? false}))))

(defn -main [& args]
  (run-server))

;(-main)
;(empty? nil)
