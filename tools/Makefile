# Install emacs init.el and git .gitconfig with "make config"
# Install the rest with "make install"

PROGS   = deblank
SCRIPTS = deblankall
EMACS   = ${HOME}/.emacs.d/init.el
GIT     = ${HOME}/.gitconfig

all:	$(PROGS)

clean:
	-rm $(PROGS)

install:	all
	-mkdir ${HOME}/bin
	cp $(PROGS) $(SCRIPTS) ${HOME}/bin

uninstall:
	cd ${HOME}/bin; rm $(PROGS) $(SCRIPTS)

$(EMACS):	init.el
	-mkdir ${HOME}/.emacs.d
	cp init.el $(EMACS)

$(GIT):		gitconfig
	cp gitconfig $(GIT)

config:	$(EMACS) $(GIT)

.PHONY:	all clean install uninstall config
