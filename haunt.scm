(use-modules (ice-9 match)
             (srfi srfi-1)
             (srfi srfi-26)
             (haunt site)
             (haunt reader)
             (haunt reader commonmark)
             (haunt builder blog)
             (haunt builder atom)
             (haunt builder assets)
             (haunt asset)
             (haunt page)
             (haunt html)
             (haunt utils)
	     (haunt post))

(define data
  '((about . "http://reuz.fr/#me")
    (email . ("mthl@reuz.fr"
              "mathieu.lirzin@nereide.fr"
              ;; "mathieu.lirzin@etu.univ-tours.fr"
              "mthl@gnu.org"))
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
                   ((about . "https://labs.nereide.fr/mthl")
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
    `(p "I am a software engineer at " ,nereide " doing research and
developpement on the Apache OFBiz ERP framework. My topics of interest
are Programming Languages, Network Based Software Architecture,
Software packaging and the World Wide Web.")))

(define free-software-desc
  (let ((gnu (xref "https://www.gnu.org/" "GNU"))
        (gnu-packages (describe-links (assq-ref data 'has-contributed)))
        (forges (describe-links (assq-ref data 'uses-forge))))
    `(p "I have been involved in the development of some "
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
       ;; (dt "University postal address")
       ;; (dd ,(xref "https://www.openstreetmap.org/way/118648970"
       ;;            "UFR Sciences et Techniques, Site universitaire de Blois, 3 place Jean Jaurès 41000 Blois."))
       (dt "Email")
       (dd ,@emails)
       (dt "PGP")
       (dd ,(iref "/mthl.asc"
                  "F2A3 8D7E EB2B 6640 5761  070D 0ADE E100 9460 4D37"))))))

(define (home-page site title)
  `((header (h1 ,title))
    (main (img (@ (src "images/mthl.png") (alt "portrait")))
          ,research-desc
          ,free-software-desc
          ,blog-desc)
    (footer ,contact-information)))

(define site
  (let ((mthl "Mathieu Lirzin"))
    (site #:title mthl
          #:domain "reuz.fr"
          #:default-metadata `((author . ,mthl)
                               (email . "mthl@reuz.fr"))
          #:readers (list commonmark-reader)
          #:builders (list
                      (page #:path "index.html"
                            #:title mthl
                            #:content home-page)
                      (static-directory "static" "")
                      (blog #:theme blog-theme #:prefix "blog")
                      (atom-feed)
                      (atom-feeds-by-tag)))))

(define (build-site site)
  "Build SITE in the appropriate build directory."
  (let* ((mthl "Mathieu Lirzin")
         (posts (read-posts "posts"
                            default-file-filter
                            (list commonmark-reader)
                            `((author . ,mthl)
                              (email . "mthl@reuz.fr"))))
         (build-dir (absolute-file-name "site")))
    (when (file-exists? build-dir)
      (delete-file-recursively build-dir)
      (mkdir build-dir))
    (for-each (match-lambda
                ((? page? page)
                 (format #t "writing page '~a'~%" (page-file-name page))
                 (write-page page build-dir))
                ((? asset? asset)
                 (format #t "copying asset '~a' → '~a'~%"
                         (asset-source asset)
                         (asset-target asset))
                 (install-asset asset build-dir))
                (obj
                 (error "unrecognized site object: " obj)))
              (flat-map (cut <> site posts)
                        (list
                         (page #:path "index.html"
                               #:title mthl
                               #:content home-page)
                         (static-directory "resources" "")
                         (blog #:theme blog-theme #:prefix "blog")
                         (atom-feed)
                         (atom-feeds-by-tag))))))
