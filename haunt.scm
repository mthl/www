(use-modules (ice-9 match)
             (srfi srfi-1)
             (haunt site)
             (haunt reader)
             (haunt reader commonmark)
             (haunt builder blog)
             (haunt builder atom)
             (haunt builder assets)
             (haunt page)
             (haunt html)
	     (haunt post))

(define data
  '((about . "http://reuz.fr/#me")
    (email . ("mthl@reuz.fr"
              "mathieu.lirzin@nereide.fr"
              "mathieu.lirzin@etu.univ-tours.fr"
              "mthl@gnu.org"
              "mthl@apache.org"))
    (contributes . (((about . "https://ofbiz.apache.org/")
                     (name . "Apache OFBiz"))))
    (has-contributed . (((about . "https://www.gnu.org/software/automake")
                         (name . "Automake"))
                        ((about . "https://www.gnu.org/software/guix")
                         (name . "Guix"))
                        ((about . "https://www.gnu.org/software/jwhois")
                         (name . "JWhois"))
                        ((about . "https://www.gnu.org/software/mcron")
                         (name . "Mcron"))
                        ((about . "https://www.gnu.org/software/shepherd")
                         (name . "Shepherd"))
                        ((about . "https://www.gnu.org/software/texinfo")
                         (name . "Texinfo"))))
    (uses-forge . (((about . "https://github.com/mthl")
                    (name . "Github"))
                   ((about . "https://gitlab.com/mthl")
                    (name . "Gitlab"))
                   ((about . "https://labs.nereide.fr/lmathieu")
                    (name . "Néréide labs"))
                   ((about . "https://savannah.gnu.org/users/mthl")
                    (name . "Savannah"))
                   ((about . "https://notabug.org/mthl")
                    (name . "Notabug"))))
    (works . ((about . "https://nereide.fr/")
              (name . "Néréide")))))

(define (stylesheet file)
  `(link (@ (rel "stylesheet") (href ,file))))

(define (anchor content uri)
  `(a (@ (href ,uri)) ,content))

(define %cc-by-sa-link
  '(a (@ (href "https://creativecommons.org/licenses/by-sa/4.0/"))
      "Creative Commons Attribution Share-Alike 4.0 International"))

(define (basic-layout site title body)
  (define body*
    (match body
      (((lxs ...) ...) lxs)
      ((xs ...) (list xs))
      (x (error x "must be an SXML element or a list of SXML elements"))))
  
  `((doctype "html")
    (html (@ (lang "en"))
          (head (meta (@ (charset "utf-8")))
                (title ,title)
                ,(stylesheet "style.css"))
          (body ,@body*))))

(define (%post-template post)
  `((h2 ,(post-ref post 'title))
    (h3 "by " ,(post-ref post 'author)
        " — " ,(date->string* (post-date post)))
    (div ,(post-sxml post))))

(define (%collection-template site title posts prefix)
  (define (post-uri post)
    (string-append (if prefix
                       (string-append "/" prefix "/")
                       "/")
                   (site-post-slug site post) ".html"))

  `((h2 "Articles")
    (ul
     ,@(map (lambda (post)
              `(li
                (a (@ (href ,(post-uri post)))
                   ,(post-ref post 'title))
                " "
                (em ,(date->string* (post-date post)))))
            (posts/reverse-chronological posts)))))

(define blog-theme
  (theme #:name "Haunt"
         #:layout basic-layout
         #:post-template %post-template
         #:collection-template %collection-template))

(define* (page #:key path title content)
  (lambda (site _)
    (let ((page (basic-layout site title (content site title))))
      (make-page path page sxml->html))))

(define* (xref href #:optional desc)
  `(a (@ (href ,href) (target "_blank") (rel "noopener"))
      ,(or desc `(code ,href))))

(define* (iref href #:optional desc)
  `(a (@ (href ,href))
      ,(or desc `(code ,href))))

(define (mail-to email)
  (iref (string-append "mailto:" email) email))

(define (dref obj)
  (xref (assq-ref obj 'about) (assq-ref obj 'name)))

(define (join lst delim)
  (match lst
    (() '())
    ((first . rest)
     (fold-right (λ (x acc) (cons* x delim acc))
                 (list first)
                 rest))))

(define* (describe-links resources #:key (mapper dref))
  (join (map mapper resources) ", "))

(define research-desc
  (let ((nereide (xref "https://nereide.fr/" "Néréide"))
        (lifat (xref "https://lifat.univ-tours.fr/" "LIFAT"))
        (bdtln (xref "https://lifat.univ-tours.fr/teams/bdtin/" "BdTln"))
        (univ (xref "https://www.univ-tours.fr/" "University of Tours"))
        (markhoff (xref "http://www.info.univ-tours.fr/~markhoff/" "Béatrice Markhoff")))
    `(p "I am a software engineer at " ,nereide " and a PhD student
at " (em "Laboratoire d'Informatique Formelle et Appliquée de
Tours") " (",lifat "), ",univ " in the " (em "Base de Données et Traitement
des Langues Naturelles") " (" ,bdtln ") team.  My PhD thesis which subject is
“Semantic Interoperability in multi-tier systems” has started in april 2019
under the supervision of " ,markhoff " with the goal of finding sound and
effective ways to compose Hypermedia APIs in the context of Information
Systems. My topics of interest are Programming Languages, Network Based
Software Architecture, Software packaging and the World Wide Web.")))

(define free-software-desc
  (let ((ofbiz (xref "https://ofbiz.apache.org/" "Apache OFBiz"))
        (gnu (xref "https://www.gnu.org/" "GNU"))
        (gnu-packages (describe-links (assq-ref data 'has-contributed)))
        (forges (describe-links (assq-ref data 'uses-forge))))
    `(p "I am contributing to some "
        ,(xref "http://dbpedia.org/resource/Free_software" "Free Software") " projects
but mainly to " ,ofbiz ". I have been involved in the development of some "
,gnu " packages: " ,@gnu-packages ". The software I am writing is
available on various repositories: " ,@forges ".")))

(define blog-desc
  `(p "I have a personal " ,(iref "/blog" "blog") " containing the weekly
reports I have made during my last "
,(xref "https://summerofcode.withgoogle.com/" "Google Summer of Code") "
experience as a student in 2017."))

(define contact-information
  (let ((emails (describe-links (assq-ref data 'email) #:mapper mail-to)))
    `((h4 "Contact information")
      (dl
       (dt "Néréide postal address")
       (dd ,(xref "https://www.openstreetmap.org/node/4999813121" "8 rue des déportés 37000 Tours"))
       (dt "University postal address")
       (dd ,(xref "https://www.openstreetmap.org/way/118648970"
                  "UFR Sciences et Techniques, Site universitaire de Blois, 3 place Jean Jaurès 41000 Blois."))
       (dt "Email")
       (dd ,@emails)
       (dt "PGP")
       (dd ,(iref "/mthl.asc"
                  "F2A3 8D7E EB2B 6640 5761  070D 0ADE E100 9460 4D37"))
       (dt "Social")
       (dd ,(xref "https://www.linkedin.com/in/mthl" "Linkedin") ", "
           ,(xref "https://mstdn.fr/@mthl/" "Mastodon"))))))

(define (home-page site title)
  `((header (h1 ,title))
    (main (img (@ (src "images/mthl.png") (alt "portrait")))
          ,research-desc
          ,free-software-desc
          ,blog-desc)
    (footer ,contact-information)))

(let ((mthl "Mathieu Lirzin"))
  (site #:title mthl
        #:domain "mthl.reuz.fr"
        #:default-metadata `((author . ,mthl)
                             (email . "mthl@reuz.fr"))
        #:readers (list commonmark-reader)
        #:builders (list
                    (page #:path "index.html"
                          #:title "Mathieu Lirzin"
                          #:content home-page)
                    (static-directory "static" "")
                    (blog #:theme blog-theme #:prefix "blog")
                    (atom-feed)
                    (atom-feeds-by-tag))))
