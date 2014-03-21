(defproject steel-plains-tcg "0.1.0-SNAPSHOT"
  :description "Steel Plains: a TCG. A hacking project for the Cape Town Clojure User Group."
  :url "https://github.com/cape-town-clojure/steel-plains-tcg"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :jvm-opts ^:replace ["-Xmx1g" "-server"]

  :dependencies [[org.clojure/clojure "1.5.1"]

                 ;; HTTP
                 [ring/ring-core "1.2.1" :exclusions [org.clojure/tools.reader]]
                 [javax.servlet/servlet-api "2.5"]
                 [fogus/ring-edn "0.2.0"]
                 [ring-anti-forgery "0.3.0"]
                 [http-kit "2.1.17"]
                 [compojure "1.1.6"]
                 ;; Datomic
                 [com.datomic/datomic-free "0.9.4532" :exclusions [com.google.guava/guava]]
                 [datomic-schema "1.0.2"]

                 ;; Both Clojure and ClojureScript
                 [prismatic/schema "0.2.0"]
                 [com.taoensso/sente "0.8.2" :exclusions [org.clojure/clojurescript]]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]

                 ;; ClojureScript
                 [org.clojure/clojurescript "0.0-2173"]
                 [prismatic/dommy "0.1.2"]
                 [om "0.5.3"]
                 [sablono "0.2.14" :exclusions [com.facebook/react]]
                 [ankha "0.1.1"]

                 ;; Tooling
                 [org.clojure/tools.nrepl "0.2.3" :exclusions [org.clojure/clojure]]
                 [environ "0.4.0"]
                 [expectations "2.0.6"]]

  :plugins [[lein-cljsbuild "1.0.2"]
            [com.keminglabs/cljx "0.3.2"]
            [lein-expectations "0.0.7"]
            [lein-autoexpect "1.2.2"]]

  :source-paths ["target/generated/clj" "src/clj"]
  :resource-paths ["resources"]
  :test-paths ["target/generated/clj" "test/clj"]

  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src/clj" ;; for macros
                    "src/cljs" "target/generated/cljs"]
     :compiler
     {:output-to "resources/public/js/main.js"
      :output-dir "resources/public/js/out"
      :preamble ["react/react.js"]
      :externs ["react/externs/react.js"]
      :optimizations :none
      :source-map true}}]}

  :cljx
  {:builds
   [{:source-paths ["src/cljx/"]
     :output-path "target/generated/clj"
     :rules :clj}
    {:source-paths ["src/cljx/"]
     :output-path "target/generated/cljs"
     :rules :cljs}]}

  :main sptcg.core)
