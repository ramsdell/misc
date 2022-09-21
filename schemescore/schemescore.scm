;; Scheme Score--A Csound Score Preprocessor

;; Scheme Score translates a score file augmented with Scheme code
;; into a Scheme program.  When the generated program is executed by a
;; Scheme interpreter, it produces a processed score file for input to
;; Csound.

;; Usage: (scmscore EXCLUDE IN-FILE OUT-FILE)
;; When EXCLUDE is true, the standard Scheme Score prolog is excluded.

;; John D. Ramsdell - February 2001

;; This is the MIT License from http://opensource.org.

;; Copyright (c) 2001 John D. Ramsdell.

;; Permission is hereby granted, free of charge, to any person
;; obtaining a copy of this software and associated documentation
;; files (the "Software"), to deal in the Software without
;; restriction, including without limitation the rights to use, copy,
;; modify, merge, publish, distribute, sublicense, and/or sell copies
;; of the Software, and to permit persons to whom the Software is
;; furnished to do so, subject to the following conditions:

;; The above copyright notice and this permission notice shall be
;; included in all copies or substantial portions of the Software.

;; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
;; EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
;; MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
;; NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
;; HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
;; WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
;; OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
;; DEALINGS IN THE SOFTWARE.

;; Main entry point
(define (scmscore exclude in-file out-file)
  (call-with-input-file in-file
    (lambda (in)
      (call-with-output-file out-file
	(lambda (out)
	  (if (not exclude)		; Add prolog if so requested
	      (write-list *prolog* out))
	  (start in out))))))

;; Main entry when the scmscore process is part of a pipe.
(define (scmscore-pipe)
  (let ((out (current-output-port)))
    (write-list *prolog* out)
    (start (current-input-port) out)))

(define *prolog*
  '((define (make-statement-printer operation)
      (lambda args
	(display operation)
	(do ((args args (cdr args)))
	    ((not (pair? args)))
	  (display " ")
	  (write (car args)))
	(newline)))
    (define f (make-statement-printer 'f))
    (define i (make-statement-printer 'i))
    (define a (make-statement-printer 'a))
    (define t (make-statement-printer 't))
    (define b (make-statement-printer 'b))
    (define v (make-statement-printer 'v))
    (define s (make-statement-printer 's))
    (define e (make-statement-printer 'e))
    (define r (make-statement-printer 'r))
    (define m (make-statement-printer 'm))
    (define n (make-statement-printer 'n))))

(define (write-list list out)
  (do ((list list (cdr list)))
      ((not (pair? list)))
    (write (car list) out)
    (newline out)))

;; Start state for preprocessing
(define (start in out)
  (let ((ch (peek-char in)))
    (cond ((eof-object? ch) #f)
	  ((char-whitespace? ch)
	   (write-char (read-char in) out)
	   (start in out))
	  ((eq? ch #\;)
	   (consume-line in out))
	  ((char-alphabetic? ch)	; Found a line of text that
	   (display "(apply " out)	; contains a statement
	   (write-char (read-char in) out)
	   (display " `(" out)
	   (line in out))
	  (else
	   (write (read in) out)
	   (start in out)))))

;; Consume end of line state
(define (consume-line in out)
  (let ((ch (read-char in)))
    (cond ((eof-object? ch) #f)
	  ((eq? ch #\newline)
	   (newline out)
	   (start in out))
	  (else
	   (consume-line in out)))))

;; Preprocess a line of text that contains a statement.
(define (line in out)
  (let ((ch (peek-char in)))
    (cond ((eof-object? ch)
	   (display "))" out)
	   #f)
	  ((or (eq? ch #\;)
	       (eq? ch #\newline))
	   (display "))" out)
	   (consume-line in out))
	  ((or (char-whitespace? ch)
	       (eq? ch #\,)		; A trick to make quasiquote
	       (eq? ch #\@))		; work right
           (write-char (read-char in) out)
           (line in out))
          (else
           (write (read in) out)
           (line in out))))
