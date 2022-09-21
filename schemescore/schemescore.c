/* Scheme Score--A Csound Score Preprocessor

Scheme Score translates a score file augmented with Scheme code into a
Scheme program.  When the generated program is executed by a Scheme
interpreter, it produces a processed score file for input to Csound.

John D. Ramsdell - January 2001

*/

/* This is the MIT License from http://opensource.org.

Copyright (c) 2001 John D. Ramsdell.

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/

#if !defined VERSION
#define VERSION "unknown"
#endif

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>

/* Tokens returned by the lexical analyzer */
typedef enum {
  EFIL,				/* end of file  */
  WSPC,				/* white space */
  NLIN,				/* end of line */
  ATOM,				/* non-list item */
  LPAR,				/* left parenthesis */
  RPAR,				/* right parenthesis */
} token_t;

static char *infile = "-";
static int lineno = 1;
static int colno = 0;

/* Print out Emacs friendly error messages */
static void
fatal_error(const char* message)
{
  fprintf(stderr, "%s:%d:%d: %s\n",
	  infile, lineno, colno, message);
  exit(1);
}

/* Lexical analyzer */

/* Strings associated with tokens are returned in the str global
   variable. */
#define STRSIZ (1 << 9)
static char str[STRSIZ];

/* The character reader can handle at most one pushed back character. */
static int pushed = 0;

static int
getch(void)
{
  if (pushed) {
    int ch = pushed;
    pushed = 0;
    return ch;
  } else {
    int ch = getchar();
    if (ch == 0) {
      fatal_error("Null input character");
    }
    /* set lineno and colno */
    if (ch == '\n') {
      colno = 0;
      lineno++;
    } else {
      colno++;
    }
    /* filter out carriage returns */
    if (ch == '\r') {
      return getch();
    } else {
      return ch;
    }
  }
}

static void
ungetch(int ch)
{
  pushed = ch;
}

static int
isdelim(int ch)
{
  switch (ch) {
  case EOF:
  case ' ':
  case '\t':
  case '\f':
  case '\v':
  case '\n':
  case '\'':
  case '`':
  case ',':
  case '@':
  case '(':
  case ')':
  case '"':
  case '#':
  case ';':
    return 1;
  default:
    return 0;
  }
}

static token_t
getstring(void)
{
  int i;
  for (i = 1; i < STRSIZ - 1; i++) {
    int ch = getch();
    switch (ch) {
    case EOF:
      fatal_error("End of file within a string");
      return ATOM;
    case '\n':
      fatal_error("End of line within a string");
      return ATOM;
    default:
      str[i] = ch;
      if (ch == '"') {
	str[i + 1] = 0;
	return ATOM;
      } else if (ch == '\\') {
	ch = getch();
	if (ch == '"' || ch == '\\') {
	  i++;
	  str[i] = ch;
	} else {
	  ungetch(ch);
	}
      }
    }
  }
  fatal_error("String too big");
  return ATOM;
}

static token_t
getatom(void)
{
  int i;
  for (i = 1; i < STRSIZ; i++) {
    int ch = getch();
    if (isdelim(ch)) {
      ungetch(ch);
      str[i] = 0;
      return ATOM;
    }
    str[i] = ch;
  }
  fatal_error("Atom too big");
  return ATOM;
}

static token_t
getsharp(void)
{
  int ch = getch();
  if (ch != '\\') {		/* The code works even for vector */
    ungetch(ch);		/* constants because # is returned */
    return getatom();	/* as an atom followed by a list. */
  } else {
    str[1] = ch;
    ch = getch();
    if (ch == EOF) {
      fatal_error("End of file within a character");
      return ATOM;
    }
    str[2] = ch;
    if (!isalpha(ch)) {
      str[3] = 0;
      return ATOM;
    } else {
      int i;
      for (i = 3; i < STRSIZ; i++) {
	int ch = getch();
	if (isdelim(ch)) {
	  ungetch(ch);
	  str[i] = 0;
	  return ATOM;
	}
	str[i] = ch;
      }
      fatal_error("Character constant too big");
      return ATOM;
    }
  }
}

static token_t
getcomment(void)
{
  int i;
  for (i = 1; i < STRSIZ - 1; i++) {
    int ch = getch();
    str[i] = ch;
    switch (ch) {
    case EOF:		/* Return NLIN so that any */
      ungetch(ch);	/* final comment will be */
      str[i] = 0;		/* copied out. */
      return NLIN;
    case '\n':
      str[i + 1] = 0;
      return NLIN;
    }
  }
  fatal_error("Comment too big");
  return ATOM;
}

static token_t
gettoken(void)
{
  int ch = getch();
  str[0] = ch;
  str[1] = 0;
  switch (ch) {
  case EOF:
    ungetch(ch);		/* Be ready to return EFIL again */
    return EFIL;
  case ' ':
  case '\t':
  case '\v':
  case '\f':
    return WSPC;
  case '\n':
    return NLIN;
  case '\'':
  case '`':
  case ',':
  case '@':
    return ATOM;
  case '(':
    return LPAR;
  case ')':
    return RPAR;
  case '"':
    return getstring();
  case '#':
    return getsharp();
  case ';':
    return getcomment();
  default:
    return getatom();
  }
}

/* Parser */

/* Parse a list minus the opening parenthesis. */
static void
list(void)
{
  printf("%s", str);
  while (1) {
    token_t t = gettoken();
    switch (t) {
    case EFIL:
      fatal_error("End of file within a list");
      return;
    case WSPC:
      printf("%s", str);
      break;
    case NLIN:
      printf("%s", str);
      break;
    case ATOM:
      printf("%s", str);
      break;
    case LPAR:
      list();
      break;
    case RPAR:
      printf("%s", str);
      return;
    default:
      fatal_error("Internal token dispatch error");
      return;
    }
  }
}

/* Parse a traditional Csound score statement minus the opening atom */
static void
line(void)
{
  char ch = str[0];
  if (isdelim(ch)) {
    fatal_error("Illegal line start character");
  }
  printf("(apply %c `(%s", ch, str + 1);
  while (1) {
    token_t t = gettoken();
    switch (t) {
    case EFIL:
      printf("))");		/* gettoken will be called again */
      return;
    case WSPC:
      printf("%s", str);
      break;
    case NLIN:
      printf("))%s", str);
      return;
    case ATOM:
      printf("%s", str);
      break;
    case LPAR:
      list();
      break;
    case RPAR:
      fatal_error("Unmatched left parenthesis");
      return;
    default:
      fatal_error("Internal token dispatch error");
      return;
    }
  }
}

/* Parse a file */
static int
parse(void)
{
  while (1) {
    token_t t = gettoken();
    switch (t) {
    case EFIL:
      return 0;
    case WSPC:
      printf("%s", str);
      break;
    case NLIN:
      printf("%s", str);
      break;
    case ATOM:
      line();
      break;
    case LPAR:
      list();
      break;
    case RPAR:
      fatal_error("Unmatched right parenthesis");
      return 1;
    default:
      fatal_error("Internal token dispatch error");
      return 1;
    }
  }
}

static const char prolog[] =
";;; Start of the standard Scheme Score prolog\n"
"(define (make-statement-printer operation)\n"
"  (lambda args\n"
"    (display operation)\n"
"    (do ((args args (cdr args)))\n"
"	((not (pair? args)))\n"
"      (display \" \")\n"
"      (write (car args)))\n"
"    (newline)))\n"
"(define f (make-statement-printer 'f))\n"
"(define i (make-statement-printer 'i))\n"
"(define a (make-statement-printer 'a))\n"
"(define t (make-statement-printer 't))\n"
"(define b (make-statement-printer 'b))\n"
"(define v (make-statement-printer 'v))\n"
"(define s (make-statement-printer 's))\n"
"(define e (make-statement-printer 'e))\n"
"(define r (make-statement-printer 'r))\n"
"(define m (make-statement-printer 'm))\n"
"(define n (make-statement-printer 'n))\n"
";;; End of the standard Scheme Score prolog\n"
"\n";

static void
usage(const char *program)
{
  fprintf(stderr,
	  "Usage:\t%s [option] [ input-file [ output-file ]]\n",
	  program);
  fprintf(stderr, "Options:\n");
  fprintf(stderr, "  -x\texclude standard Scheme Score prolog\n");
  fprintf(stderr, "  -v\tprint Scheme Score version information\n");
  fprintf(stderr, "  -h\tprint this message\n");
}

static void
version(void)
{
  fprintf(stderr, "%s", "Scheme Score version " VERSION "\n");
}

int
main(int argc, char **argv)
{
  int flag = 0;
  char *outfile = "-";

  /* Check for a command line option */
  if (argc > 1 && argv[1][0] == '-' && argv[1][1] != '\0') {
    if (!strcmp("-v", argv[1]) || !strcmp("--version", argv[1])) {
      version();
      return 0;
    }
    if (!strcmp("-h", argv[1]) || !strcmp("--help", argv[1])) {
      usage(argv[0]);
      return 0;
    }
    if (strcmp("-x", argv[1]) && strcmp("--exclude", argv[1])) {
      fprintf(stderr, "Unrecognized command line option: %s\n", argv[1]);
      usage(argv[0]);
      return 1;
    }
    flag++;
  }

  /* Get file names on the command line */
  switch (argc - flag - 1) {
  case 2:
    outfile = argv[flag + 2];
    /* fall thru okay */
  case 1:
    infile = argv[flag + 1];
    /* fall thru okay */
  case 0:
    break;
  default:
    fprintf(stderr, "Wrong number of command line arguments\n");
    usage(argv[0]);
    return 1;
  }

  if (strcmp("-", infile)) {
    if (!freopen(infile, "r", stdin)) {
      fprintf(stderr, "Cannot open %s for input\n", infile);
      return 1;
    }
  }

  if (strcmp("-", outfile)) {
    if (!freopen(outfile, "w", stdout)) {
      fprintf(stderr, "Cannot open %s for output\n", outfile);
      return 1;
    }
  }

  if (!flag) {
    printf("%s", prolog);
  }

  return parse();
}
