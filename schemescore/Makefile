# A GNU makefile for Scheme Score, a Csound score preprocessor.

TARGETS = scmscore example.aiff

%.scm:	%.ssc
	./scmscore $*.ssc $*.scm

%.sco:	%.scm
	guile -s $*.scm > $*.sco

%.aiff:	%.orc %.sco
	./csound -d -A -o $*.aiff $*.orc $*.sco > $*.txt

%.wav:	%.orc %.sco
	./csound -d -W -o $*.wav $*.orc $*.sco > $*.txt

all:	 $(TARGETS)

clean:
	-rm $(TARGETS) *.txt
