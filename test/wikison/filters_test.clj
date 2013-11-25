(ns wikison.filters-test
  (:require [clojure.test :refer :all]
            [wikison.filters :refer :all]))

(deftest has-child?-test-no
  (testing "section with no subsection have no child"
    (let [in [[:text "some text"] [:title "no child section"]] 
          ex nil
          ou (has-child? in)]
      (is (= ex ou)))))

(deftest has-child?-test-yes
  (testing "section with subsection have childs"
    (let [in [[:text "some text"] [:title "no child section"] 
              [:subs1 [:sub1 [:title "section"] [:text "some"]]]] 
          ex true
          ou (has-child? in)]
      (is (= ex ou)))))

(deftest text-val-test-yes
  (testing "section element with a text value"
    (let [in [[:title "title"] [:some-tag "tag"] [:text "text"]]
          ex "text"
          ou (text-val in)]
      (is (= ex ou)))))

(deftest text-val-test-no
  (testing "section element with no text value"
    (let [in [[:title "title"] [:some-tag "tag"] [:toxt "text"]]
          ex nil
          ou (text-val in)]
      (is (= ex ou)))))

(deftest empty-sec?-test-yes
  (testing "a section with no child and blank text is empty"
    (let [in [ [:title "empty"] [:text "\n\n"] [:tag "no section"] ]
          ex true
          ou (empty-sec? in)]
      (is (= ex ou)))))

(deftest empty-sec?-test-no
  (testing "a section with non blanl text is not empty"
    (let [in [ [:title "empty"] [:text "text"] [:tag "no section"] ]
          ex false
          ou (empty-sec? in)]
      (is (= ex ou)))))

(deftest empty-sec?-test-no
  (testing "a section with subsection is not empty"
    (let [in [ [:title "empty"] [:tag "no section"] [:subs1 [:sub1 [:text "a"] [:title "title"]]] ]
          ex false
          ou (empty-sec? in)]
      (is (= ex ou)))))


