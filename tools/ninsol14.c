/* Program ninsol14 by John D. Ramsdell

   This program computes the total daily irradiation received at the
   top of the atmosphere for a given latitude and time in the year (in
   Wm-2).  User documentation is included as a comment in this source
   file.

   Copyright (C) 2021 John D. Ramsdell

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

/*
   This program is a fairly direct translation of a Fortran program
   written by Andre Berger available at:

   https://www.elic.ucl.ac.be/modx/index.php?id=83

   The mathematics implemented by the program is at that site.

   Andre Berger, "Long-Term Variations of Daily Insoluation and
   Quaternary Climate Changes", Journal of the Atmosphere Sciences,
   Vol. 35, No. 12, Dec. 1978.

   This program improves on the Fortran program by adding a mode that
   reads and writes data using tab separated values.  This addition
   eases the task of graphing the results.  Also, the Fortran program
   reads tabular values used to compute orbital positions from a file,
   but this program includes the tables in the program.  The structure
   of this program follows the Fortran program so as to aid anyone
   that takes the time to verify the correctness of the translation.
   Comments written in all-caps have been copied from the Fortran
   program.

   This program is maintained and distributed at

   https://github.com/ramsdell/misc
*/

/*
			      USER GUIDE

  The ninsol14 program is interactive.  When run in its default mode,
  prompts describe the input expected by the program.  The output
  describes the answers computed.  The origin of time (t = 0)
  corresponds to 1950.  Time is given in units of 1000 years, and time
  is negative when in the past.  The true longitude of sun is given in
  degrees, and its origin (tls = 0) is the vernal equinox.  The
  insolation during a day is in Watts per meter squared.

  When run in tab separated values mode (-t), prompts are suppressed
  and the output associated with one query is printed as one line of
  tab separated values.  This mode is for generating output that can
  be easily consumed by graphing programs.

  The input is copied to the output, and then computed values are
  added.  The output fields are:

   1. latitude in degrees (input)
   2. time in units of 1000 years (input)
   3. option (iopt = 1 or 2) for specifying the day of the year (input)
   4. number of terms used for eccentricity (input) [use 19]
   5. number of terms used for obliquity (input) [use 47]
   6. number of terms used for precession (input) [use 78]
   7. eccentricity
   8. precession parameter
   9. longitude of perhilion
  10. obliquity

  if iopt = 1 then

  11. true longitude of sun (input)
  12. day insolation
  13. length of day

  if iopt = 2 then

  ll. month (input)
  12. day (input)
  13. true longitude of sun
  14. mean longitude of sun for month-day
  15. day insolation
  16. length of day

  $ ninsol14 -h
  Compute orbital positions and insolation
  using Berger's 1978 algorithm

  Usage: ninsol14 [options] [input]
  Options:
    -o file -- output to file (default is standard output)
    -t      -- output tab separated values and suppress prompts
    -v      -- print version number
    -h      -- print this message
*/

/*
          PROGRAM BERGER 78 version 2014

     THIS SOLUTION OF BERGER 1978 IS VALID ONLY FOR 1.000.000 YEARS
     CENTERED ON PRESENT-DAY.
     FOR LONGER PERIOD THE SOLUTION 1990 MUST BE USED.
     (CONTACT BERGER FOR THIS 1990 SOLUTION)

     This program is interactive
       It computes astronomical elements and insolation for one date
       It can be modified but be carefull and check after modifications made

     Solar constant is in program at beginning of section 4
     Insolation is given in Wm-2

   PLEASE REFER TO :
      BERGER A. 1978. A SIMPLE ALGORITHM TO COMPUTE LONG TERM
                      VARIATIONS OF DAILY OR MONTHLY INSOLATION
                      CONTR. 18  INST OF ASTRONOMY AND GEOPHYSICS
                      UNIVERSITE CATHOLIQUE DE LOUVAIN.
                      LOUVAIN-LA-NEUVE    BELGIUM.

      BERGER A. 1978. LONG TERM VARIATIONS OF DAILY INSOLATION AND
                      QUATERNARY CLIMATIC CHANGES
                      J. OF ATMOSPHERIC SCIENCES  35  2362-2367
 */

#include <math.h>
#include <stdio.h>
#include <unistd.h>

// Version as year-month
#define VERSION "21.10"
#define PROG "ninsol14"

// Constants

#define PI M_PI
#define PIR (PI / 180.0)
#define PIRR (PIR / 3600.0)
#define XND 365.0
#define STEP (360.0 / XND)
#define TEST 0.0001

// Tables that appear in the annex of Berger1978

/*
   1.EARTH ORBITAL ELEMENTS : ECCENTRICITY           ECC   TABLE 1
***************************   PRECESSIONAL PARAMETER PRE
                              OBLIQUITY              XOB   TABLE 2
                              GENERAL PRECESSION     PRG
                              LONGITUDE PERIHELION   PERH  TABLE 3
*/

// Eccentricity

#define NEF (sizeof(ae) / sizeof(double))

static double ae[] = {
  0.01860798,
  0.01627522,
  -0.01300660,
  0.00988829,
  -0.00336700,
  0.00333077,
  -0.00235400,
  0.00140015,
  0.00100700,
  0.00085700,
  0.00064990,
  0.00059900,
  0.00037800,
  -0.00033700,
  0.00027600,
  0.00018200,
  -0.00017400,
  -0.00012400,
  0.00001250,
};

static double be[] = {
  4.2072050,
  7.3460910,
  17.8572630,
  17.2205460,
  16.8467330,
  5.1990790,
  18.2310760,
  26.2167580,
  6.3591690,
  16.2100160,
  3.0651810,
  16.5838290,
  18.4939800,
  6.1909530,
  18.8677930,
  17.4255670,
  6.1860010,
  18.4174410,
  0.6678630,
};

static double ce[] = {
  28.620089,
  193.788772,
  308.307024,
  320.199637,
  279.376984,
  87.195000,
  349.129677,
  128.443387,
  154.143880,
  291.269597,
  114.860583,
  332.092251,
  296.414411,
  145.769910,
  337.237063,
  152.092288,
  126.839891,
  210.667199,
  72.108838,
};

// Obliquity

#define NOB (sizeof(aob) / sizeof(double))

#define XOD 23.320556

static double aob[] = {
  -2462.2214466,
  -857.3232075,
  -629.3231835,
  -414.2804924,
  -311.7632587,
  308.9408604,
  -162.5533601,
  -116.1077911,
  101.1189923,
  -67.6856209,
  24.9079067,
  22.5811241,
  -21.1648355,
  -15.6549876,
  15.3936813,
  14.6660938,
  -11.7273029,
  10.2742696,
  6.4914588,
  5.8539148,
  -5.4872205,
  -5.4290191,
  5.1609570,
  5.0786314,
  -4.0735782,
  3.7227167,
  3.3971932,
  -2.8347004,
  -2.6550721,
  -2.5717867,
  -2.4712188,
  2.4625410,
  2.2464112,
  -2.0755511,
  -1.9713669,
  -1.8813061,
  -1.8468785,
  1.8186742,
  1.7601888,
  -1.5428851,
  1.4738838,
  -1.4593669,
  1.4192259,
  -1.1818980,
  1.1756474,
  -1.1316126,
  1.0896928,
};

static double bob[] = {
  31.609974,
  32.620504,
  24.172203,
  31.983787,
  44.828336,
  30.973257,
  43.668246,
  32.246691,
  30.599444,
  42.681324,
  43.836462,
  47.439436,
  63.219948,
  64.230478,
  1.010530,
  7.437771,
  55.782177,
  0.373813,
  13.218362,
  62.583231,
  63.593761,
  76.438310,
  45.815258,
  8.448301,
  56.792707,
  49.747842,
  12.058272,
  75.278220,
  65.241008,
  64.604291,
  1.647247,
  7.811584,
  12.207832,
  63.856665,
  56.155990,
  77.448840,
  6.801054,
  62.209418,
  20.656133,
  48.344406,
  55.145460,
  69.000539,
  11.071350,
  74.291298,
  11.047742,
  0.636717,
  12.844549,
};

static double cob[] = {
  251.9025,
  280.8325,
  128.3057,
  292.7252,
  15.3747,
  263.7951,
  308.4258,
  240.0099,
  222.9725,
  268.7809,
  316.7998,
  319.6024,
  143.8050,
  172.7351,
  28.9300,
  123.5968,
  20.2082,
  40.8226,
  123.4722,
  155.6977,
  184.6277,
  267.2772,
  55.0196,
  152.5268,
  49.1382,
  204.6609,
  56.5233,
  200.3284,
  201.6651,
  213.5577,
  17.0374,
  164.4194,
  94.5422,
  131.9124,
  61.0309,
  296.2073,
  135.4894,
  114.8750,
  247.0691,
  256.6114,
  32.1008,
  143.6804,
  16.8784,
  160.6835,
  27.5932,
  348.1074,
  82.6496,
};

// General precession in longitude

#define XOP 3.392506
#define PRM 50.439273

#define NOP (sizeof(aop) / sizeof(double))

static double aop[] = {
  7391.0225890,
  2555.1526947,
  2022.7629188,
  -1973.6517951,
  1240.2321818,
  953.8679112,
  -931.7537108,
  872.3795383,
  606.3544732,
  -496.0274038,
  456.9608039,
  346.9462320,
  -305.8412902,
  249.6173246,
  -199.1027200,
  191.0560889,
  -175.2936572,
  165.9068833,
  161.1285917,
  139.7878093,
  -133.5228399,
  117.0673811,
  104.6907281,
  95.3227476,
  86.7824524,
  86.0857729,
  70.5893698,
  -69.9719343,
  -62.5817473,
  61.5450059,
  -57.9364011,
  57.1899832,
  -57.0236109,
  -54.2119253,
  53.2834147,
  52.1223575,
  -49.0059908,
  -48.3118757,
  -45.4191685,
  -42.2357920,
  -34.7971099,
  34.4623613,
  -33.8356643,
  33.6689362,
  -31.2521586,
  -30.8798701,
  28.4640769,
  -27.1960802,
  27.0860736,
  -26.3437456,
  24.7253740,
  24.6732126,
  24.4272733,
  24.0127327,
  21.7150294,
  -21.5375347,
  18.1148363,
  -16.9603104,
  -16.1765215,
  15.5567653,
  15.4846529,
  15.2150632,
  14.5047426,
  -14.3873316,
  13.1351419,
  12.8776311,
  11.9867234,
  11.9385578,
  11.7030822,
  11.6018181,
  -11.2617293,
  -10.4664199,
  10.4333970,
  -10.2377466,
  10.1934446,
  -10.1280191,
  10.0289441,
  -10.0034259,
};

static double bop[] = {
  31.609974,
  32.620504,
  24.172203,
  0.636717,
  31.983787,
  3.138886,
  30.973257,
  44.828336,
  0.991874,
  0.373813,
  43.668246,
  32.246691,
  30.599444,
  2.147012,
  10.511172,
  42.681324,
  13.650058,
  0.986922,
  9.874455,
  13.013341,
  0.262904,
  0.004952,
  1.142024,
  63.219948,
  0.205021,
  2.151964,
  64.230478,
  43.836462,
  47.439436,
  1.384343,
  7.437771,
  18.829299,
  9.500642,
  0.431696,
  1.160090,
  55.782177,
  12.639528,
  1.155138,
  0.168216,
  1.647247,
  10.884985,
  5.610937,
  12.658184,
  1.010530,
  1.983748,
  14.023871,
  0.560178,
  1.273434,
  12.021467,
  62.583231,
  63.593761,
  76.438310,
  4.280910,
  13.218362,
  17.818769,
  8.359495,
  56.792707,
  8.448301,
  1.978796,
  8.863925,
  0.186365,
  8.996212,
  6.771027,
  45.815258,
  12.002811,
  75.278220,
  65.241008,
  18.870667,
  22.009553,
  64.604291,
  11.498094,
  0.578834,
  9.237738,
  49.747842,
  2.147012,
  1.196895,
  2.133898,
  0.173168,
};

static double cop[] = {
  251.9025,
  280.8325,
  128.3057,
  348.1074,
  292.7252,
  165.1686,
  263.7951,
  15.3747,
  58.5749,
  40.8226,
  308.4258,
  240.0099,
  222.9725,
  106.5937,
  114.5182,
  268.7809,
  279.6869,
  39.6448,
  126.4108,
  291.5795,
  307.2848,
  18.9300,
  273.7596,
  143.8050,
  191.8927,
  125.5237,
  172.7351,
  316.7998,
  319.6024,
  69.7526,
  123.5968,
  217.6432,
  85.5882,
  156.2147,
  66.9489,
  20.2082,
  250.7568,
  48.0188,
  8.3739,
  17.0374,
  155.3409,
  94.1709,
  221.1120,
  28.9300,
  117.1498,
  320.5095,
  262.3602,
  336.2148,
  233.0046,
  155.6977,
  184.6277,
  267.2772,
  78.9281,
  123.4722,
  188.7132,
  180.1364,
  49.1382,
  152.5268,
  98.2198,
  97.4808,
  221.5376,
  168.2438,
  161.1199,
  55.0196,
  262.6495,
  200.3284,
  201.6651,
  294.6547,
  99.8233,
  213.5577,
  154.1631,
  232.7153,
  138.3034,
  204.6609,
  106.5938,
  250.4676,
  332.3345,
  27.3039,
};

// Put the tables into the form used by the algorithm
static void
init_tables() {

  // Eccentricity
  for (int i = 0; i < NEF; i++) {
    be[i] *= PIRR;
    ce[i] *= PIR;
  }

  // Obliquity
  for (int i = 0; i < NOB; i++) {
    bob[i] *= PIRR;
    cob[i] *= PIR;
  }

  // Precession
  for (int i = 0; i < NOP; i++) {
    bop[i] *= PIRR;
    cop[i] *= PIR;
  }
}

const static char prompt[] =
  " ENTER SUCCESSIVELY : \n"
  "PHI : LATITUDE\n"
  "T   : TIME (IN 1000 YEAR\n"
  "      0 FOR THE PRESENT\n"
  "       NEGATIVE IN THE PAST\n"
  "       POSITIVE IN THE FUTURE\n"
  "IOPT: = 1 TRUE LONG. SUN TLS\n"
  "      = 2  MONTH MA - DAY J\n"
  "NEFF  NOBB  NOPP :  NUMBER  OF TERMS KEPT FOR COMPUTATION        OF\n"
  "THE EARTH ORBITAL ELEMENTS.\n"
  "THEY ARE 19 47 78 AT THE MAXIMUM\n"
  "THEY CAN BE REDUCED TO 19 18 9 RESPECTIVELY FOR A MINIMUM ACCURACY\n";

// Purge characters until the end of line is reached.
static void
readnl() {
  for (;;) {
    int ch = getchar();
    if (ch == EOF || ch == '\n')
      return;
  }
}

static int
nday(int ma, int ja) {
  static int njm[] = {31,28,31,30,31,30,31,31,30,31,30,31};
  int nd = 0;
  int m = ma - 1;
  for (int i = 0; i < m; i++) {
    nd += njm[i];
  }
  nd += ja;
  return nd;
}

static void
daysin(double ecc, double xl, double so, double dlam,
       double phi, double sf, double *ww, double *dayl) {
  double rphi = phi * PIR;
  double ranv = (dlam - xl) * PIR;
  double rau = (1.0 - ecc * ecc) / (1.0 + ecc * cos(ranv));
  double s = sf / rau / rau;
  double rlam = dlam * PIR;
  double sd = so * sin(rlam);
  double cd = sqrt(1.0 - sd * sd);
  double rdelta = atan(sd / cd);
  double delta = rdelta / PIR;
  double sp = sd * sin(rphi);
  double cp = cd * cos(rphi);
  double aphi = fabs(phi);
  double adelta = fabs(delta);
  /*
   SINGULARITY FOR APHI=90 AND DELTA=0
   PARTICULAR CASES FOR PHI=0  OR  DELTA=0
   */
  double tt = fabs(aphi - 90.0);
  if (tt <= TEST && adelta <= TEST) {
    /* POLAR CONTINUAL NIGHT OR W=0  DAYL=0 */
    *dayl = 0.0;
    *ww = 0.0;
    return;
  }
  if (adelta <= TEST) {
    /* EQUINOXES */
    *dayl = 12.0;
    *ww = s * cos(rphi);
    return;
  }
  if (aphi <= TEST) {
    /* EQUATOR */
    *dayl = 12.0;
    *ww = s * cos(rdelta);
    return;
  }
  double at = 90.0 - adelta;
  double spd = phi * delta;
  if (aphi <= at || spd == 0.0) {
    /* DAILY SUNRISE AND SUNSET */
    double tp = -sp / cp;
    double stp = sqrt(1.0 - tp * tp);
    double rdayl = acos(tp);
    *dayl = 24.0 * rdayl / PI;
    *ww = s * (rdayl * sp + cp * stp);
  } else if (spd < 0.0) {
    /* POLAR CONTINUAL NIGHT OR W=0  DAYL=0 */
    *dayl = 0.0;
    *ww = 0.0;
  } else {
    /* POLAR CONTINUAL DAY */
    *dayl = 24.0;
    *ww = s * sp * PI;
  }
}

static int
ninsol14(int tsv) {

  init_tables();

  /*
    2.INPUT PARAMETERS : LATITUDE PHI - TIME T - OPTION IOPT
**********************
         IF IOPT=1  TRUE LONG.SUN TLS
         IF IOPT=2  MONTH MA - DAY JA

      NEFF  NOBB  NOPP  NUMBER OF TERMS KEPT FOR COMPUTATION OF
                         EARTH ORBITAL ELEMENTS
      THEY CAN BE REDUCED TO 19,18,9 RESPECTIVELY
                 FOR A MINIMUM ACCURACY
  */
  if (!tsv)
    printf(" THIS PROGRAMME COMPUTES THE TOTAL DAILY "
	   "IRRADIATION RECEIVED AT THE TOP \n"
	   "OF THE ATMOSPHERE FOR A GIVEN LATITUDE "
	   "AND TIME IN THE YEAR (IN WM-2)\n");

  for (;;) {			/* Main loop */
    double phi, t;
    int iopt, neff, nobb, nopp, rc;

    if (!tsv)
      printf("%s", prompt);
    rc = scanf("%lf %lf %d %d %d %d",
	       &phi, &t, &iopt, &neff, &nobb, &nopp);
    if (rc == EOF)
      return 0;
    if (rc != 6) {
      fprintf(stderr, "Bad input\n");
      return 1;
    }

    // Check input
    if (neff < NEF) neff = NEF;	/* Weird, why input neff */
    if (neff > NEF) neff = NEF;	/* when it's going to be NEF? */
    if (nobb < 18) nobb = NOB;
    if (nobb > NOB) nobb = NOB;
    if (nopp < 9) nopp = NOP;
    if (nopp > NOP) nopp = NOP;

    if (tsv)
      printf("%f\t%f\t%d\t%d\t%d\t%d\t",
	     phi, t, iopt, neff, nobb, nopp);

    /*
   3.NUMERICAL VALUE FOR ECC PRE XOB
************************************
       T IS NEGATIVE FOR THE PAST   T IS IN 1000 YEARS
    */
    t *= 1000.0;		/* The origin is 1950. */
    double xes = 0.0;
    double xec = 0.0;
    for (int i = 0; i < neff; i++) {
      double arg = be[i] * t + ce[i];
      xes += ae[i] * sin(arg);
      xec += ae[i] * cos(arg);
    }

    // Eccentricity
    double ecc = sqrt(xes * xes + xec * xec);

    double tra = fabs(xec);
    double rp;
    if (tra <= 1.0e-8 || xec == 0.0) {
      if (xes < 0.0)		/* Label 10 */
	rp = 1.5 * PI;
      else if (xes == 0.0)
	rp = 0.0;
      else
	rp = PI / 2.0;
    } else {
      rp = atan(xes / xec);
      if (xec < 0.0)
	rp += PI;
      else if (xec < 0.0)
	rp += 2.0 * PI;
    }
    double perh = rp / PIR;	/* Label 13 */

    double prg = PRM * t;
    for (int i = 0; i < nopp; i++) {
      double arg = bop[i] * t + cop[i];
      prg += aop[i] * sin(arg);
    }
    prg = prg / 3600.0 + XOP;
    perh += prg;
    while (perh < 0.0) perh += 360.0;
    while (perh >= 360.0) perh -= 360.0;
    // Longitude of perihelion

    // Precession parameter
    double pre = ecc * sin(perh * PIR);

    // Obliquity
    double xob = XOD;
    for (int i = 0; i < nobb; i++) {
      double arg = bob[i] * t + cob[i];
      xob += aob[i] / 3600.0 * cos(arg);
    }

    /*
   4.DAILY INSOLATION
*********************

   OPTION SOLAR DATE - CALENDAR DATE
       DAILY INSOLATION IN LY DAY(-1)    OR    KJ M(-2) DAY(-1)
                  IF S0 IN LY MIN(-1)    OR    W M(-2)
                     TAU = 24*60 MIN     OR    24*60*60 SEC / 1000

                        IN W M(-2)
                  IF S0 IN W M(-2)      AND    TAU=1.0

    */
    double ss = 1368.0;

    if (tsv)
      printf("%f\t%f\t%f\t%f\t", ecc, pre, perh, xob);
    else {
      printf(" LATITUDE =%6.1f   Solar constant=%8.2f"
	     "  Number days in year=%10.4f\n", phi, ss, XND);
      printf(" TIME =%10.1f   ECCENTRICITY =%8.5f\n"
	     "                    PREC. PARAM. =%8.5f\n"
	     "                    LONG. PERH. =%7.1f\n"
	     "                    OBLIQUITY =%7.3f\n\n",
	     t, ecc, pre, perh, xob);
    }

    double tau = 1.0;
    double sf = tau * ss / PI;
    double so = sin(xob * PIR);
    double xl = perh + 180.0;
    double tls, ww, dayl;

    switch (iopt) {
    case 1:			// TRUE LONG. SUN TLS
      /*
   4.1 SOLAR DATE
-----------------
       CONSTANT INCREMENT OF TRUE LONGITUDE OF SUN TLS
       ORIGIN IS VERNAL EQUINOX
       IF TLS=I*30 MID-MONTH IS NOW AROUND 21
       TLS=0,30,...300,330 RESPECTIVELY FOR MARCH ... JANUARY, FEBRUARY
       */
      if (!tsv)
	printf(" ENTER TRUE LONGITUDE OF SUN (TLS)\n"
	       "TLS=0,30,...300,330 RESPECTIVELY FOR MARCH "
	       "... JANUARY, FEBRUARY\n");
      if (1 != scanf("%lf", &tls)) {
	fprintf(stderr, "Missing true longitude of sun\n");
	return 1;
      }
      readnl();
      if (tls < 0.0 || tls >= 360.0)
	printf("Bad true longitude of sun: %9.3f\n", tls);

      daysin(ecc, xl, so, tls, phi, sf, &ww, &dayl);
      if (tsv)
	printf("%f\t", tls);
      else
	printf(" TRUE LONG. SUN =%9.3f\n", tls);
      break;

    case 2:			// MONTH MA - DAY JA
      /*
   4.2 CALENDAR DATE  MA-JA
--------------------
      ND  NUMBER OF THIS DAY IN A YEAR OF 365 DAYS
      XLAM = MEAN LONG. SUN FOR TRUE LONG. = 0
      DLAMM = MEAN LONG. SUN FOR MA-JA

   OUTPUT : WW=LY/DAY  OR  KJ M(-2) DAY(-1) or Wm-2
            DAYL=LENGTH OF DAY (HOURS)
      */
      if (!tsv)
	printf(" ENTER CALENDAR DATE  (MONTH MA - DAY JA) \n");
      int ma, ja;
      if (2 != scanf("%d %d", &ma, &ja)) {
	fprintf(stderr, "Missing calendar date\n");
	return 1;
      }
      readnl();
      if (ma < 1 || ma > 12) {
	fprintf(stderr, "Bad month: %d\n", ma);
	return 1;
      }
      if (ja < 1 || ja > 31) {
	fprintf(stderr, "Bad day: %d\n", ma);
	return 1;
      }
      int nd = nday(ma, ja);
      double xllp = xl * PIR;
      double xee = ecc * ecc;
      double xse = sqrt(1.0 - xee);
      double xlam = (ecc / 2.0 + ecc * xee / 8.0) * (1.0 + xse) * sin(xllp)
	- xee / 4.0 * (0.5 + xse) * sin(2.0 * xllp)
	+ ecc * xee / 8.0 * (1.0 / 3.0 + xse) * sin(3.0 * xllp);
      xlam = 2.0 * xlam / PIR;
      double dlamm = xlam + (nd - 80) * STEP;
      double anm = dlamm - xl;
      double ranm = anm * PIR;
      double xec = xee * ecc;
      double ranv = ranm + (2.0 * ecc - xec / 4.0) * sin(ranm)
	+ 5.0 / 4.0 * ecc * ecc * sin(2.0 * ranm)
	+ 13.0 / 12.0 * xec * sin(3.0 * ranm);
      double anv = ranv / PIR;
      tls = anv + xl;
      daysin(ecc, xl, so, tls, phi, sf, &ww, &dayl);
      if (tsv)
	printf("%d\t%d\t%f\t%f\t", ma, ja, tls, dlamm);
      else
	printf(" MONTH =%3d   DAY =%3d   TLS =%9.3f   DLAM=%9.3f\n",
	       ma, ja, tls, dlamm);
      break;
    default:
      fprintf(stderr, "Bad option %d\n", iopt);
      return 1;
    }
    if (tsv)
      printf("%f\t%f\n", ww, dayl);
    else {
      printf(" DAY INSOL. =%9.4f WM(-2)    LENGTH DAY =%6.2f HOURS\n\n",
	     ww, dayl);
      for (int i = 0; i < 70; i++) putchar('*');
      puts("\n");
    }
  }
}

// Usage message
static void
usage()
{
  fprintf(stderr,
          "Compute orbital positions and insolation\n"
	  "using Berger's 1978 algorithm\n\n"
	  "Usage: %s [options] [input]\n"
	  "Options:\n"
	  "  -o file -- output to file (default is standard output)\n"
	  "  -t      -- output tab separated values and suppress prompts\n"
	  "  -v      -- print version number\n"
	  "  -h      -- print this message\n",
	  PROG);
}

int
main(int argc, char **argv)
{
  extern char *optarg;
  extern int optind;

  char *output = NULL;
  int tsv = 0;

  for (;;) {
    int c = getopt(argc, argv, "o:thv");
    if (c == -1)
      break;
    switch (c) {
    case 'o':
      output = optarg;
      break;
    case 't':
      tsv = 1;
      break;
    case 'v':
      printf("%s %s\n", PROG, VERSION);
      return 0;
    case 'h':
      usage();
      return 0;
    default:
      usage();
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
    usage();
    return 1;
  }

  if (output && !freopen(output, "w", stdout)) {
    perror(output);
    return 1;
  }

  return ninsol14(tsv);
}
