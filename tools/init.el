;;; Ramsdell's simple Emacs init file
;;; Place in ~/.emacs.d/init.el

;; For Mac
;; (set-frame-font "Courier New 18")

;; Always end a file with a newline
(setq require-final-newline t)

(setq visible-bell t)

(setq load-path (cons (expand-file-name "~/el") load-path))

(setq Info-default-directory-list
      (cons (expand-file-name "~/info/")
	    Info-default-directory-list))

(global-set-key "\M-\C-y" 'compile)
(defun myshell ()
  (interactive)
  (shell)
  (delete-other-windows))
(global-set-key "\C-z" 'myshell)
(global-set-key "\M-#" 'goto-line)
(global-set-key "\C-xz" 'gnus)

(display-time)

;; Ignore local variables in files.
(setq enable-local-variables nil)

;; start text and mail modes in auto fill mode
(add-hook 'text-mode-hook 'turn-on-auto-fill)
;; and flyspell mode
(add-hook 'text-mode-hook 'flyspell-mode)
