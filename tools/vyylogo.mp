%% The Verification Yin-Yang Logo in METAPOST

% Original comment in the METAFONT version of the VYY logo follows.

%% The Verification Yin-Yang Logo and the Yin-Yang Symbol
%%
%% Programmed in METAFONT by John D. Ramsdell---October 1993,
%% The MITRE Corporation, Bedford,~MA.
%%
%% The program expects {\tt ht\#} and only {\tt ht\#} to be defined in a
%% parameter file.  All other parameters are derived from this
%% one.

%% This version has not been tuned for low resolution printers.  In
%% particular, the letters could be improved.

% The single parameter in METAPOST is h.
h=250pt;

prologue:=1;

% s		= 1/9w;			% Extra space at the left and the right.
s		= 0;			% No extra space!
w		= h + 2s;		% Width of symbol.
c_d		= 1/25h;		% Diameter of circle pen.
l_h		= 1/5h;			% Letter heights.
diag		= 1/10h;		% Size of eyes.

%% Figure

beginfig(-1);

 bot y1 = 0;  top y5 = h;
 y2 - top y1 = y3 - y2 = y4 - y3 = bot y5 - y4;
 y6 = y3; y7 = y2; y8 = y4;

 x1 = x3 = x5 = x7 = x8 = 1/2w;
 x2 = x1 - (y2 - top y1);
 x4 = x3 + y4 - y3;
 x6 = x3 + bot y5 - y3;

 erase fill fullcircle scaled (y5 - y1) shifted z3;

 pickup pencircle scaled c_d;

 draw fullcircle scaled (y5 - y1) shifted z3;

 fill z5{right} .. z6{down} .. z1{left} .. z2{up} .. z3{right}
  .. z4{up} .. (bot z5){left} .. cycle;

 %% labels(range 1 thru 8);

%% For all symbol

 pickup pencircle scaled c_d;

 x_height = 8/13l_h;
 forall_w = 11/13l_h;
 x10 = x1; y10 = y8 - 1/2l_h;
 top y9 = l_h + y10; y11 = y9;
 lft x9 = x1 - 1/2forall_w; rt x11 = forall_w + lft x9;
 y12 = y13 = x_height + y10;
 z12=whatever[z9,z10]; z13=whatever[z10,z11];
 draw z9--z10--z11;	% diagonals
 draw z12--z13;		% bar
 %% labels(range 9 thru 13);

%% Lambda

 lambda_w = 8/13l_h;
 y18 = y16 = y7 - 1/2l_h;
 top y14 = l_h + y16;  x14 = 1/13l_h + x18;
 lft x18 = x1 - 1/2lambda_w; rt x16 = lambda_w + lft x18;
 y15 = y14 - 2/13l_h; x15 = x14 + 5/26l_h;
 z17 = whatever[z15,z16]; y17 = y16 + 6/13l_h;

 erase draw z14{right}...z15---z16;
 erase draw z17--z18;
 %% labels(range 14 thru 18);

 endfig;
bye;
