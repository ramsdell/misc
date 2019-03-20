#include <stdio.h>
#include <ctype.h>

int main(int argc, char **argv)
{
  int i;
  for (i = 1; i < argc; i++) {
    int lineno = 1;
    int colno = 0;
    char *path = argv[i];
    FILE *in = fopen(path, "r");
    if (!in) {
      perror(path);
      return 1;
    }
    for (;;) {
      int ch = getc(in);
      if (ch == EOF)
	break;
      if (ch == '\n') {
	lineno++;
	colno = 0;
      }
      else {
	colno++;
	if (!isprint(ch) && !isspace(ch))
	  printf("%s:%d:%d Non-printable character found: %d\n",
		 path, lineno, colno, ch);
      }
    }
    fclose(in);
  }
  return 0;
}
