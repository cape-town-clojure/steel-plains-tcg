(defproject steel-plains-tcg "0.1.0-SNAPSHOT"
  :description "Steel Plains: a TCG. A hacking project for the Cape Town Clojure User Group."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :jvm-opts ^:replace ["-Xmx1g" "-server"]

  :dependencies [[ring/ring-core "1.2.0"]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.5.2"]
                 [http-kit "2.1.16"]
                 [compojure "1.1.6"]
                 [fogus/ring-edn "0.2.0"]
                 [com.datomic/datomic-free "0.9.4532"]
                 [com.taoensso/sente "0.8.2"]
                 [datomic-schema "1.0.2"]
                 [org.clojure/tools.nrepl "0.2.3" :exclusions [org.clojure/clojure]]
                 [clojure-complete "0.2.3"]
                 [environ "0.4.0"]]

  :plugins [[lein-cljsbuild "1.0.2"]]

  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources"]

  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src/clj" "src/cljs"]
     :compiler
     {:output-to "resources/public/js/main.js"
      :output-dir "resources/public/js/out"
      :optimizations :none
      :source-map true}}]}
  :main sptcg.core)
