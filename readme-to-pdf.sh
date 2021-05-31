#!/bin/bash
grip README.md --export doc/index.html
wkhtmltopdf --enable-local-file-access doc/index.html doc/david.burkhart-piano.pdf