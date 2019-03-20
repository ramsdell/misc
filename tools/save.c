/* save -- John D. Ramsdell -- May 1988
   Revised May 1994 to use ANSI C features. */

/* Adds a version number to a file name.  The file xxx/yyy.zzz is moved to
   xxx/yyy__dd.zzz, were dd is the lowest integer that can be used without
   duplicating the file name.  A new copy of xxx/yyy.zzz then is created. */

/* This program assumes the commands "cp" and "mv" obtained using the
   system command are those described in the POSIX 1003.2 manual. */

#define MAX_VERSIONS 100
/* The value for MAX_VERSIONS must be consistent with the decimal field
   used in the format string given to sprintf to generate file names
   and the generated field size. */
#define GENERATED_FIELD_SIZE 4
#define CMD_SIZE 20

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

/* find_period returns a pointer to the last period in a file name
   when that period occurs in its base name and is not the first
   character of the base name.  The base name is the file name that
   remains after deleting the longest prefix which ends with "/".
   When no period is found in the base name, find_period returns a
   pointer to the end of the string. */
char *
find_period(char *s)
{
  char *p = NULL;	     /* pointer to period at end of routine */

  if (*s == '.') s++;	 /* Skip periods that begin name components */

  for (; *s; s++)
    if (*s == '/') {
      p = NULL;
      if (s[1] == '.') s++; /* Skip periods that begin name components */
    }
    else if (*s == '.') p = s;

  if (!p) p = s;		/* No period found in base name */
				/* return a pointer to the end of the string */
  return p;
}

/* save a file by renaming the file and then copying the renamed file
   back to the original file.  Returns zero on success. */
int
make_file_copy(char *file_name, char *new_file_name, char *command)
{
  int version; int status;
  char *dot = find_period(file_name);
  char *field = new_file_name + (dot - file_name);

  strcpy(new_file_name, file_name);

  for (version = 0; version < MAX_VERSIONS; version++) {
    sprintf(field, "__%02d", version);
    strcpy(field + GENERATED_FIELD_SIZE, dot);
    if (access(new_file_name, F_OK) < 0) {
      sprintf(command, "mv '%s' '%s'&&cp '%s' '%s'\n",
	      file_name, new_file_name,
	      new_file_name, file_name);
      status = system(command);
      if (status != 0) {
	fprintf(stderr,
		"Failed while attempting to execute the command:\n%s",
		command);
	return status;
      }
      return 0;
    }
  }
  fprintf(stderr, "Failed to generate an unused file name.\n");
  return 1;
}

/* The save routine gives up on file names which contain a quote, or
   it allocates space and calls make_file_copy that does the real work. */
int
save(char *file_name)
{
  size_t len = strlen(file_name);
  char *new_file_name;
  char *command;
  char *s;
  int status;

  /* if file_name contains the quote character, give up. */
  for (s = file_name; *s; s++)
    if (*s == '\'') return 1;

  new_file_name = (char *)malloc(len + GENERATED_FIELD_SIZE + 1);
  /* size of the generated command is the size of the two file names
     plus the space for the cp and mv command. */
  command = (char *)malloc(4 * len + 2 * GENERATED_FIELD_SIZE + CMD_SIZE);

  status = make_file_copy(file_name, new_file_name, command);

  free(new_file_name);
  free(command);

  return status;
}

int
main(int argc, char *argv[])
{
  int exit_status = 0;

  if (argc == 1) {
    fprintf(stderr, "Usage:\t%s filename [filename]*\n", *argv);
    return 0;
  }

  for (argv++; *argv != NULL; argv++)
    exit_status |= save(*argv);

  return exit_status;
}
