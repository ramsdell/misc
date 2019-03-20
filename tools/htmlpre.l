/* Hey emacs, use -*- mode:c -*- for this file. */

/* htmlpre -- strips code from the PRE elements in an HTML document.
              In encode mode (-e), it translates the characters
              &, <, >, and " into character entities so that
              the output can be included into a PRE element
              of an HTML document.

   John D. Ramsdell
   June 2000

   Patched in May 2001 to correctly follow the SGML ling break rule,
   which is that a line break immediately following a start tag must
   be ignored, as must a line break immediately before an end tag.

   Patched in October 2001 to add install and uninstall targets to the
   Makefile.

		      Htmlpre License Agreement

	      Copyright (C) 2000, John D. Ramsdell.
			 All Rights Reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

* Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

* Neither name of John D. Ramsdell; nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT
HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
DAMAGE.

*/

%{
int emitting = 0;
int maybe_putchar(int c) 	{
  return emitting ? putchar(c) : c;
}
int maybe_puts(const char *s) 	{
  return emitting ? fputs(s, stdout) : 0;
}
int maybe_putnl() 		{
  int c = '\n';
  if (emitting == 2) {
    emitting = 1;
    return c;
  }
  else
    return emitting ? putchar(c) : c;
}
%}

%%

"<"[Pp][Rr][Ee][^>]*">"$	{ emitting = 2; }
"<"[Pp][Rr][Ee][^>]*">"		{ emitting = 1; }
&amp;				{ maybe_putchar('&'); }
&lt;				{ maybe_putchar('<'); }
&gt;				{ maybe_putchar('>'); }
&quot;				{ maybe_putchar('"'); }
&nbsp;				{ maybe_putchar(' '); }
\n				{ maybe_putnl(); }
.				{ maybe_puts(yytext); }
^"</"[Pp][Rr][Ee]">"		{ emitting = 0; }
"</"[Pp][Rr][Ee]">"		{ emitting = 0; putchar('\n'); }

%%

#include <stdio.h>
#include <string.h>

int encode(void)
{
  for (;;) {
    int c;
    c = getchar();
    switch (c) {
    case EOF:
      return 0;			/* return errno != 0; */
    case '&':
      fputs("&amp;", stdout);
      break;
    case '<':
      fputs("&lt;", stdout);
      break;
    case '>':
      fputs("&gt;", stdout);
      break;
    case '"':
      fputs("&quot;", stdout);
      break;
    default:
      putchar(c);
    }
  }
}

int main(int argc, char **argv)
{
  int offset = 0;		/* set extract from PRE mode */
  if (argc > 1 && argv[1][0] == '-' && argv[1][1] != 0) {
    if (strcmp("-e", argv[1]) == 0)
      offset = 1;		/* set encode mode */
    else
      offset = argc;		/* force an error */
  }
  switch (argc - offset) {
  case 3:
    if (strcmp("-", argv[2 + offset]) != 0
	&& freopen(argv[2 + offset], "w", stdout) == 0) {
      fprintf(stderr, "Cannot open %s for writing.\n", argv[2 + offset]);
      break;
    }
    /* fall thru okay */
  case 2:
    if (strcmp("-", argv[1 + offset]) != 0
	&& freopen(argv[1 + offset], "r", stdin) == 0) {
      fprintf(stderr, "Cannot open %s for reading.\n", argv[1 + offset]);
      break;
    }
    /* fall thru okay */
  case 1:
    if (offset)
      return encode();
    while (yylex());
    return 0;
  }
  fprintf(stderr,
	  "Usage: %s [-e] [ input [ output ] ]\n"
	  "no flags  strips text from the PRE elements in an HTML document\n"
	  "-e\t  encodes text for inclusion into a PRE element\n",
	  argv[0]);
  return 1;
}
