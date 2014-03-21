(ns sptcg.tests.core
  (:use expectations)
  (:require [schema.core :as s]))

(defmacro expect-schema
  [s a]
  `(expect ~a (s/validate ~s ~a)))
