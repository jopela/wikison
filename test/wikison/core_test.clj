(ns wikison.core-test
  (:require [clojure.test :refer :all]
            [wikison.core :refer :all]))

; an example api call result
(def api-result {:langlinks [
{:lang "da", :* "Arrangement in Gray and Black: The Artist's Mother"}
{:lang "de", :* "Arrangement in Grau und Schwarz: Porträt der Mutter des Künstlers"}
{:lang "es", :* "Retrato de la madre del artista"}
{:lang "et", :* "Whistleri ema"}
{:lang "fa", :* "مادر ویسلر"}
{:lang "fr", :* "Arrangement en gris et noir n°1"}
{:lang "he", :* "סידור באפור ושחור: אמו של האמן"}
{:lang "la", :* "Mater pictoris (Whistler)"}
{:lang "nl", :* "Arrangement in Gray and Black, No. 1"}
{:lang "pl", :* "Matka Whistlera"}],
                :pagelanguage "en",
                :title "Whistler's Mother",
                :thumbnail {:source "https://upload.wikimedia.org/wikipedia/commons/1/1b/Whistlers_Mother_high_res.jpg", :width 9999, :height 8897}, 
                :extract "Arrangement in Grey and Black No.1,is a painting . 
                 It is now one of the most famous works by an American artist 
                 outside the United States. \n\n\n== History ==\n\nAnna McNeill
                         Whistler posed for the painting while living in London
                         with her son. \n\n\n== Appearances in American museums
                         ==\nWhistler's Mother occasionally appears in the 
                         United States.\n\n\n== In popular culture ==\nThe 
                         painting has been featured or mentioned in numerous
                         works of fiction and within pop culture.\n\n\n== In music ==\n
                         Whistler and particularly this painting had a profound
                         effect on Claude Debussy, a contemporary French 
                         composer.",
                 :fullurl "https://en.wikipedia.org/wiki/Whistler%27s_Mother",
                 :pageid 225516})

(def whistlers-mother
  {:url         "https://en.wikipedia.org/wiki/Whistler%27s_Mother"
   :title       "Whistler's Mother"
   :pageid      225516
   :abstract    "Arrangement in Grey and Black ..."
   :depiction   "https://upload.wikimedia.org/wikipedia/commons/1/1b/Whistlers_Mother_high_res.jpg"
   :lang        "en"
   :other-langs ["da" "de" "es" "et" "fa" "fr" "he" "la" "nl" "pl"]
   :sections [{:title "History" :text "Anna McNeill Whistler ..." } 
              {:title "Appearances in American museums" :text "Whistler's Mother occasionally appears" }
              {:title "In popular culture" :text "The painting has been"}
              {:title "In music" :text "Whistler and particularly"}]
   })


; test-extract tests.
(deftest parser-simple-1
  (testing "extract containing only an abstract"
    (let [in (wiki-parser (slurp "./test/wikison/extracts/simple-test-1.txt"))
          ex {:abstract "Abstract only article.\n"}
          ou (text-eval in)]
      (is (= ex ou)))))

(deftest parser-simple-2
  (testing "abstract + some sections"
    (let [in (wiki-parser (slurp "./test/wikison/extracts/simple-test-2.txt"))
          ex {:abstract "Article with a single section.\n"
              :sections [{:title " Single Section " :text "text.\n"}]}
          ou (text-eval in)]
      (is (= ex ou)))))

(deftest parser-simple-3
  (testing "several sections"
    (let [in (wiki-parser (slurp "./test/wikison/extracts/simple-test-3.txt"))
          ex {:abstract "Article with multiple sections.\n"
              :sections [{:title " Section 1 " :text "text 1.\n"}
                         {:title " Section 2 " :text "text 2.\n"}
                         {:title " Section 3 " :text "text 3.\n"}]}
          ou (text-eval in)]
      (is (= ex ou)))))

(deftest parser-simple-4
  (testing "Section with subsections"
    (let [in (wiki-parser (slurp "./test/wikison/extracts/simple-test-4.txt"))
          ex {:abstract "Article with subsection.\n\n"
              :sections [{:title " Section 1 " 
                          :text "will contain a sub.\n\n"
                          :sections [{:text "Text.\n"
                                      :title " Subsection 1 "}]}]}
          ou (text-eval in)]
      (is (= ex ou)))))

; is that unreadable or what?
(deftest parser-simple-5
  (testing "All levels of indentation"
    (let [in (wiki-parser (slurp "./test/wikison/extracts/simple-test-5.txt"))
          ex {:abstract "All levels.\n\n"
              :sections [{:title " S1 "
                          :sections 
                          [{:title " SS1 "
                                      :sections 
                            [{:title " SSS1 "
                                                  :sections 
                              [{:title " SSSS1 "
                                                              
                                :sections [{:title " SSSSS1 "
                                                                          
                                            :sections [{:title 
                                                      " SSSSSS1 "}]}]}]}]}]}]} 
          ou (text-eval in)]
      (is (= ex ou)))))

(deftest parser-ru-simple-1
  (testing "abstract only with russian char"
    (let [in (wiki-parser (slurp "./test/wikison/extracts/ru-simple-test-1.txt"))
          ex {:abstract "привет мир от русских коды символов!\n"}
          ou (text-eval in)]
      (is (= ex ou)))))

; tougher test case.
(deftest edge-test-1
  (testing "Abstract that contain code that is section-like (e.g: 1 == 1)"
    (let [in (wiki-parser (slurp "./test/wikison/extracts/edge-test-1.txt"))
          ex {:abstract "Complex\n" 
              :sections [{:title " S1 " :text "in C : 1 == 1, in clojure (= 1 1).\n"}]}
          ou (text-eval in)]
      (is (= ex ou)))))

; other function
(deftest article-test
  (testing "json document represents the article"
    (let [in  "https://en.wikipedia.org/wiki/Whistler's_Mother"
          ex  whistlers-mother 
          ou  (article in) ]
      (is (= (ex :url) (ou :url)))
      (is (= (ex :title) (ou :title))))))

(deftest article-title-test
  (testing "title of a page from english wikipedia"
    (let [in "http://en.wikipedia.org/wiki/Our_Lady_of_the_Don"
          ex "Our_Lady_of_the_Don"
          ou (article-title in)]
      (is (= ex ou)))))

(deftest api-url-test
  (testing "api-url for wikipedia url"
    (let [in "http://en.wikipedia.org/wiki/Montreal"
          ex "http://en.wikipedia.org/w/api.php"
          ou (api-url in)]
      (is (= ex ou)))))

(deftest simple-prop-extract-test
  (testing "simple property extraction from request result on MediaWiki API"
    (let [ex (select-keys whistlers-mother [:url :title :pageid :lang])
          ou (simple-prop-extract api-result)]
      (is (= ex ou)))))

(deftest languages-extract-test
  (testing "language extraction from request result on MediaWiki API"
    (let [ex (select-keys whistlers-mother [:other-langs])
          ou (languages-extract api-result)]
      (is (= ex ou)))))

(deftest thumbnail-extract-test
  (testing "thumbnail extraction from request result on MediaWiki API"
    (let [ex (select-keys whistlers-mother [:depiction])
          ou (thumbnail-extract api-result)]
      (is (= ex ou)))))


