/*
The deblank program removes trailing blanks and tabs from each line,
as well as double blank lines from the standard input and writes the
new file to the standard output.  The input and output files can be
given using command line arguments.

With the option "-j", the deblank program operates in join mode.  It
joins all the lines within a paragraph to make one line for each
paragraph.  It can be used to enter content from a text file into a
web form.

This program is often used with the following shell script:

#! /bin/sh
# Removes trailing spaces and tabs from each line,
# as well as double blank lines.

for i
do
  if test -f "$i" -a -w "$i"
  then
    mv "$i" "$i".bak
    deblank -o "$i" "$i".bak
  fi
done

John D. Ramsdell -- October 2001.

Copyright (C) 2013 John D. Ramsdell

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use, copy,
modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.
*/

#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#define SIZE (1<<10)

/* count of newlines seen but not yet printed */

static int nls = 0;

/* join lines within paragraph option selected? */

static int join = 0;

static void
denewline(int c)
{
  if (c == '\n') {
    nls++;
    return;
  }
  if (join) {			/* join lines within a paragraph */
    switch (nls) {
    case 0:
      break;
    case 1:		        /* found line break within a paragraph */
      putc(' ', stdout);
      nls = 0;
      break;
    default:			/* found paragraph break */
      putc('\n', stdout);
      putc('\n', stdout);
      nls = 0;
    }
  }
  else {			/* normal operation */
    if (nls > 2)
      nls = 2;			/* drop extra blank lines */
    for (; nls > 0; nls--) putc('\n', stdout);
  }
  putc(c, stdout);
}

/* handle white space at line end elimination */

static char buf[SIZE];		/* contains white space whose */
static size_t limit = 0;	/* disposition is unknown */

static void
deblank(int c)
{
  size_t i;
  switch (c) {
  case ' ':
  case '\t':
    if (limit >= SIZE) {
      for (i = 0; i < limit; i++) /* handle buffer overflow */
	denewline(buf[i]);	/* by flushing the buffer */
      limit = 0;
    }
    buf[limit++] = c;		/* store white space */
    return;
  case '\n':
    limit = 0;			/* drop extra white space */
    break;
  default:
    for (i = 0; i < limit; i++)	/* write white space that */
      denewline(buf[i]);	/* turns out to be needed */
    limit = 0;
  }
  denewline(c);
}

static int
filter()			/* deblanking is driven here */
{
  for (;;) {
    int c = getc(stdin);
    switch (c) {
    case EOF:
      putc('\n', stdout);  /* make sure there is one final new line */
      return 0;
    case '\r':
      break;			/* drop carriage return characters */
    default:
      deblank(c);
    }
  }
}

/* Generic filtering main and usage routines. */

static void
usage(const char *prog)
{
  fprintf(stderr,
	  "Usage: %s [options] [input]\n"
	  "Options:\n"
	  "  -o file -- output to file (default is standard output)\n"
	  "  -j      -- join lines within a paragraph into one line\n"
	  "  -h      -- print this message\n",
	  prog);
}

int
main(int argc, char **argv)
{
  extern char *optarg;
  extern int optind;

  char *output = NULL;

  for (;;) {
    int c = getopt(argc, argv, "o:jh");
    if (c == -1)
      break;
    switch (c) {
    case 'o':
      output = optarg;
      break;
    case 'j':
      join = 1;
      break;
    case 'h':
      usage(argv[0]);
      return 0;
    default:
      usage(argv[0]);
      return 1;
    }
  }

  switch (argc - optind) {
  case 0:			/* Use stdin */
    break;
  case 1:
    if (!freopen(argv[optind], "r", stdin)) {
      perror(argv[optind]);
      return 1;
    }
    break;
  default:
    fprintf(stderr, "Bad arg count\n");
    usage(argv[0]);
    return 1;
  }

  if (output && !freopen(output, "w", stdout)) {
    perror(output);
    return 1;
  }

  return filter();
}
