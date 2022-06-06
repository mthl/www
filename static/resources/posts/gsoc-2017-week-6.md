---
title: Javascript for info-style navigation - Week 6
creator: Mathieu Lirzin
date: 2017-07-10T11:36:21+02:00
subject:
    - gsoc
    - texinfo
    - javascript
    - npm
    - distribution
---

This is an update on the work I am doing this summer for my [Google Summer of Code](https://summerofcode.withgoogle.com/projects/#6199074135998464).  Please see [the first article](./gsoc-2017-week-1.md) of this serie of reports for a general introduction on what this project is about.

## Distribution

While I initialy started working on implementing the global regexp search feature which is accessible via the `s` keys.  My mentors proposed to start thinking on how to make the Javascript UI usable for others.  In other words thinking about ways to distribute the software and providing instructions for package maintainers to reuse it for their own manual.

While the ideal solution would be to simply add an extra option to`makeinfo` this is not currenly possible given that the html output produced by `makeinfo` is not well suited for working with Javascript code.  For now I have been using the Kawa HTML manual as a basis of my work.  This manual is generated using the `--docbook` output of `makeinfo` and then processed using `xslt` which produces an HTML document using the [polyglot markup](https://dev.w3.org/html5/html-polyglot/html-polyglot.html) format.

To allow others to reuse my work I have integrated this build process inside the development repository.  This allows checking if Javascript UI works with other texinfo manuals.  Since my code will likely to be reused by project following GNU Standards, it was natural to use the [Autotools](https://www.gnu.org/software/automake/manual/html_node/Autotools-Introduction.html) which I am comfortable with.  Here is the Makefile snippet that is reponsible of the build:

```makefile
build_html_pages = \
  $(XSLT) --path "$(DOCBOOK_XSL_DIR)/epub3:$(srcdir)/style" \
    --stringparam base.dir ebook/OEBPS/ \
    --stringparam html.script info.js \
    --stringparam html.stylesheet info.css \
    --stringparam chunker.output.encoding UTF-8 \
    --stringparam generate.section.toc.level 0 \
    --stringparam generate.index 1 \
    --stringparam use.id.as.filename 1 \
    --stringparam autotoc.label.in.hyperlink 0 \
    --stringparam chunker.output.indent yes \
    --stringparam chunk.first.sections 1 \
    --stringparam chunk.section.depth 1 \
    --stringparam chapter.autolabel 0 \
    --stringparam chunk.fast 1 \
    --stringparam toc.max.depth 4 \
    --stringparam toc.list.type ul \
    --stringparam toc.section.depth 3 \
    --stringparam chunk.separate.lots 1 \
    --stringparam chunk.tocs.and.lots 1 \
    info-epub.xsl $${xml_file} && \
  sed -e '/<footer>/,/<.footer>/d' <ebook/OEBPS/bk01-toc.xhtml \
    >ebook/OEBPS/ToC.xhtml && \
  rm ebook/OEBPS/bk01-toc.xhtml && \
  for file in ebook/OEBPS/*.xhtml; do \
    sed -e '/<?xml .*>/d' -e '/<script/s|/>|> </script>|' -i $$file; done && \
  mv ebook/OEBPS/index.xhtml ebook/OEBPS/index.html && \
  cp style/info.css ebook/OEBPS/info.css && \
  cp info.js ebook/OEBPS/ && \
  rm -rf $@ && \
  mv ebook/OEBPS $@; \
  rm -rf ebook

.texi.xml:
	$(MAKEINFO) -I=$(srcdir) --docbook $< -o $@

examples/hello-html: examples/hello/hello.xml
	$(AM_V_GEN)xml_file=examples/hello/hello.xml; \
	$(build_html_pages)
```

It relies both on standard and custom `XSL` stylesheets that are respectively included in the `$(DOCBOOK_XSL_DIR)` and `$(srcdir)/style` directories.  The custom stylesheets has been directly taken from Kawa repository.  The transformation is done using `$(XSLT)` which need to be defined by Autoconf in the `configure.ac` file by checking the presence of the `xsltproc` program on the system:

```sh
AC_PATH_PROG([XSLT], [xsltproc])
if test "x$ac_cv_path_XSLT" = "x"; then
   AC_MSG_ERROR([xsltproc not found])
fi
```

The problem with this solution besides depending on another tool like `xsltproc` is that it relies on docbook XSL stylesheets to be installed and I don't know a good way to reliably check there presence on the system so for now we provide a mandatory `--with-xsl-stylesheets` configure option to define the `$(DOCBOOK_XSL_DIR)` variable.

```sh
AC_ARG_WITH([docbook-stylesheets],
  [AS_HELP_STRING([--with-docbook-stylesheets=DIR],
    [define the directory containing xsl stylesheets])],
  [with_docbook_stylesheets="$withval"],
  [AC_MSG_ERROR([--with-docbook-stylesheets option must be defined])])

AC_SUBST([DOCBOOK_XSL_DIR], [$with_docbook_stylesheets])
```

Besides minor issues with the Javascript code that has been easily fixed, the integration with the [GNU Hello](https://www.gnu.org/software/hello/) manual has been successful.

## Build process

Since the beginning of this project, I have written my Javascript code using ES6 modules.  As you may have notice in the previous Makefile snippet, The Javascript code is provided to the HTML by a single `info.js` file.   This is because browsers don't support modules and as a consequence those have to be bundled before being deployed.  Additionally to improve the expressiveness and readability of my code, I have been using the latest ES6 features such as `let/const`, arrow functions, and classes in my code.  However to be usable across browsers, such code has be "transpiled" meaning compiled down to ES5 syntax to be usable in older browsers.

The bundling and transpiling is done using `rollup` and `buble` tools.  The problem with those tools is that they are distributed using NPM which doesn't integrate well with the conservative practice of distributing software through tarballs and requiring the build process to work even without internet connexion.  For example if we were bundling the whole `node_modules` directory inside the tarball, the issue would be that this directory doesn't include the actual sources of the dependencies but only some transpiled/bundled/minified artefacts.  As a consequence this would not comply with GPL requirement of including the "source" inside the distribution.  An alternative would be to require users to globally install thoses dependencies and let Autoconf find them.  The problem with that solution is that Javascript packages tends to move fast and break backward compatibility regularly, so the users would likely get non working builds.

I will pass the details, but the fact is that integrating the NPM way of doing things in the traditional source based tarball model results in a complex build process that for this particular project it doesn't worth the benefits of the tools it provides.  For that reason I have fallback to using basic ES5 syntax and a single `info.js` script that doesn't require any external tool to be installed and deployed.  NPM is still used for fetching some tools such as a linter and a minifier.  However those tools are not required by the build process and are only here for the developper convenience.

## Portability

One key requirement for this project, is that the manuals should be able to properly work even in older browsers.  Until now I have only been providing some polyfills for prototype methods that are not widely deployed for example the [String.prototype.includes](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/includes) method.  While this allows us to improve the portability without impacting the readability of the code this is not sufficient.  A good complementary strategy is to check for more basic features (for example the presence of all ES5 array methods) and if those are not available to fallback to the basic HTML navigation without Javascript.  This strategy is not perfect and can't reasonably be complete by checking every browser features combinaison.   As a consequence it is still possible for the users to get a UI failure, but if some care is taken with the selection of feature tests, those failures should be reasonably rare. In order to help I have decided to use [modernizr](https://modernizr.com/) which generates a feature scanning script based on some presets.

## Next step

the `modernizr` global variable which contains the result of the features tests, needs to be used inside `info.js` to provide its portability benefits.  Regarding the build of the HTML manuals there some minor issues need to be fixed, such as the `next` navigation command that doesn't work from the main page.

## Follow the developpement

I have updated the live demo of the Kawa manual which is available [here](https://www.gnu.org/software/texinfo/gsoc-2017-js-example/kawa)

The development of this project is done in public.  You can checkout the "gsoc-2017" branch of the [Git repository](https://git.savannah.gnu.org/git/texinfo.git) to see what is the current state of the project.
