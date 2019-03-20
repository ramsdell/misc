#include <stdio.h>
#include <unistd.h>

static int
hascr(char **argv, int quiet)
{
  int exit_code = 1;
  while (*argv) {
    char *path = *argv++;
    FILE *in = fopen(path, "r");
    if (!in) {
      perror(path);
      return 1;
    }
    for (;;) {
      int ch = getc(in);
      if (ch == EOF)
	break;
      else if (ch == '\r') {
	exit_code = 0;
	if (!quiet)
	  printf("%s\n", path);
	else
	  return exit_code;
	break;
      }
    }
    fclose(in);
  }
  return exit_code;
}

static void
usage(const char *prog)
{
  fprintf(stderr,
	  "Usage: %s [options] [file]...\n"
	  "Prints the name of the files"
	  " that contain a carriage return character.\n"
	  "Options:\n"
	  "  -q      -- do not write anything to standard output\n"
	  "  -h      -- print this message\n"
	  "Exit status is 0 if a carriage return is found, otherwise 1.\n",
	  prog);
}

int
main(int argc, char **argv)
{
  extern char *optarg;
  extern int optind;

  int quiet = 0;

  for (;;) {
    int c = getopt(argc, argv, "hq");
    if (c == -1)
      break;
    switch (c) {
    case 'q':
      quiet = 1;
      break;
    case 'h':
      usage(argv[0]);
      return 0;
    default:
      usage(argv[0]);
      return 1;
    }
  }

  return hascr(argv + optind, quiet);
}
