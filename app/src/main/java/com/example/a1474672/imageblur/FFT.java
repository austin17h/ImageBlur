package com.example.a1474672.imageblur;
import java.lang.Math;
public class FFT {



    /*  SECTIONS OF THIS FILE,
     *   i.e. => public void fft(double[] x, double[] y)
     *  adapted from MEAPsoft. (license below)
     */

    /*
     *  Copyright 2006-2007 Columbia University.
     *
     *  This file is part of MEAPsoft.
     *
     *  MEAPsoft is free software; you can redistribute it and/or modify
     *  it under the terms of the GNU General Public License version 2 as
     *  published by the Free Software Foundation.
     *
     *  MEAPsoft is distributed in the hope that it will be useful, but
     *  WITHOUT ANY WARRANTY; without even the implied warranty of
     *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     *  General Public License for more details.
     *
     *  You should have received a copy of the GNU General Public License
     *  along with MEAPsoft; if not, write to the Free Software
     *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
     *  02110-1301 USA
     *
     *  See the file "COPYING" for the text of the license.
     */

// https://www.ee.columbia.edu/~ronw/code/MEAPsoft/doc/html/FFT_8java-source.html
// https://algs4.cs.princeton.edu/99scientific/FFT.java.html

        public final static double TWOPI = 6.283185307179586476925287;

        int n_x, m_x, n_y, m_y;

        double ifft_scale_factor;

        // Lookup tables.  Only need to recompute when size of FFT changes.
        double[] cos_x;
        double[] cos_y;

        double[] sin_x;
        double[] sin_y;


        public FFT(int n_cols) {
            this.n_y = n_cols;
            this.m_y = (int) (Math.log(n_cols) / Math.log(2));


            // Make sure n_cols is a power of 2
            if (n_cols != (1 << m_y))
                throw new RuntimeException("FFT length must be power of 2");

            // precompute tables
            cos_y = new double[n_cols / 2];
            sin_y = new double[n_cols / 2];

            for (int i = 0; i < n_cols / 2; i++) {
                cos_y[i] = Math.cos(-2 * Math.PI * i / n_cols);
                sin_y[i] = Math.sin(-2 * Math.PI * i / n_cols);
            }
        }

        public FFT(int n_cols, int n_rows) {

            // ----------------------------
            // COLUMN DATA
            this.n_y = n_cols;
            this.m_y = (int) (Math.log(n_cols) / Math.log(2));

            // Make sure n_cols is a power of 2
            if (n_cols != (1 << m_y))
                throw new RuntimeException("FFT length must be power of 2");

            // precompute tables
            cos_y = new double[n_cols / 2];
            sin_y = new double[n_cols / 2];

            for (int i = 0; i < n_cols / 2; i++) {
                cos_y[i] = Math.cos(-2 * Math.PI * i / n_cols);
                sin_y[i] = Math.sin(-2 * Math.PI * i / n_cols);
            }

            // ----------------------------
            // ROW DATA
            this.n_x = n_rows;
            this.m_x = (int) (Math.log(n_rows) / Math.log(2));

            // Make sure n_cols is a power of 2
            if (n_rows != (1 << m_x))
                throw new RuntimeException("FFT length must be power of 2");

            // precompute tables
            cos_x = new double[n_rows / 2];
            sin_x = new double[n_rows / 2];

            for (int i = 0; i < n_rows / 2; i++) {
                cos_x[i] = Math.cos(-2 * Math.PI * i / n_rows);
                sin_x[i] = Math.sin(-2 * Math.PI * i / n_rows);
            }

            this.ifft_scale_factor = 1. / (n_x * n_y);

        }


        /***************************************************************
         * fft.c
         * Douglas L. Jones
         * University of Illinois at Urbana-Champaign
         * January 19, 1992
         * http://cnx.rice.edu/content/m12016/latest/
         *
         *   fft: in-place radix-2 DIT DFT of a complex input
         *
         *   input:
         * n: length of FFT: must be a power of two
         * m: n = 2**m
         *   input/output
         * x: double array of length n with real part of data
         * y: double array of length n with imag part of data
         *
         *   Permission to copy and use this program is granted
         *   as long as this header is included.
         ****************************************************************/

        public void fft2(double[][] real, double[][] imag) {

            int i, j, k, n1, n2, a;
            double c, s, e, t1, t2;

            // PHASE 1, FFT THE COLUMNS

            for (int indx_col = 0; indx_col < n_y; indx_col++) {

                // Bit-reverse
                j = 0;
                n2 = n_x / 2;
                for (i = 1; i < n_x - 1; i++) {
                    n1 = n2;
                    while (j >= n1) {
                        j = j - n1;
                        n1 = n1 / 2;
                    }
                    j = j + n1;

                    if (i < j) {
                        t1 = real[i][indx_col];
                        real[i][indx_col] = real[j][indx_col];
                        real[j][indx_col] = t1;
                        t1 = imag[i][indx_col];
                        imag[i][indx_col] = imag[j][indx_col];
                        imag[j][indx_col] = t1;
                    }
                }

                // FFT
                n1 = 0;
                n2 = 1;

                for (i = 0; i < m_x; i++) {
                    n1 = n2;
                    n2 = n2 + n2;
                    a = 0;

                    for (j = 0; j < n1; j++) {
                        c = cos_x[a];
                        s = sin_x[a];
                        a += 1 << (m_x - i - 1);

                        for (k = j; k < n_x; k = k + n2) {
                            t1 = c * real[k + n1][indx_col] - s * imag[k + n1][indx_col];
                            t2 = s * real[k + n1][indx_col] + c * imag[k + n1][indx_col];
                            real[k + n1][indx_col] = real[k][indx_col] - t1;
                            imag[k + n1][indx_col] = imag[k][indx_col] - t2;
                            real[k][indx_col] = real[k][indx_col] + t1;
                            imag[k][indx_col] = imag[k][indx_col] + t2;
                        }
                    }
                }
            }

            // PHASE 2, FFT THE ROWS

            for (int indx_row = 0; indx_row < n_x; indx_row++) {

                // Bit-reverse
                j = 0;
                n2 = n_y / 2;
                for (i = 1; i < n_y - 1; i++) {
                    n1 = n2;
                    while (j >= n1) {
                        j = j - n1;
                        n1 = n1 / 2;
                    }
                    j = j + n1;

                    if (i < j) {
                        t1 = real[indx_row][i];
                        real[indx_row][i] = real[indx_row][j];
                        real[indx_row][j] = t1;
                        t1 = imag[indx_row][i];
                        imag[indx_row][i] = imag[indx_row][j];
                        imag[indx_row][j] = t1;
                    }
                }

                // FFT
                n1 = 0;
                n2 = 1;

                for (i = 0; i < m_y; i++) {
                    n1 = n2;
                    n2 = n2 + n2;
                    a = 0;

                    for (j = 0; j < n1; j++) {
                        c = cos_y[a];
                        s = sin_y[a];
                        a += 1 << (m_y - i - 1);

                        for (k = j; k < n_y; k = k + n2) {
                            t1 = c * real[indx_row][k + n1] - s * imag[indx_row][k + n1];
                            t2 = s * real[indx_row][k + n1] + c * imag[indx_row][k + n1];
                            real[indx_row][k + n1] = real[indx_row][k] - t1;
                            imag[indx_row][k + n1] = imag[indx_row][k] - t2;
                            real[indx_row][k] = real[indx_row][k] + t1;
                            imag[indx_row][k] = imag[indx_row][k] + t2;
                        }
                    }
                }
            }
        }

        public void ifft2(double[][] real, double[][] imag) {

            int i, j, k, n1, n2, a;
            double c, s, e, t1, t2;

            // PHASE 1, FFT THE COLUMNS


            for (int indx_col = 0; indx_col < n_y; indx_col++) {

                //  first, take the conjugate
                for (i = 0; i < n_x; i++) {
                    imag[i][indx_col] *= -1.;
                }

                // Bit-reverse
                j = 0;
                n2 = n_x / 2;
                for (i = 1; i < n_x - 1; i++) {
                    n1 = n2;
                    while (j >= n1) {
                        j = j - n1;
                        n1 = n1 / 2;
                    }
                    j = j + n1;

                    if (i < j) {
                        t1 = real[i][indx_col];
                        real[i][indx_col] = real[j][indx_col];
                        real[j][indx_col] = t1;
                        t1 = imag[i][indx_col];
                        imag[i][indx_col] = imag[j][indx_col];
                        imag[j][indx_col] = t1;
                    }
                }

                // FFT
                n1 = 0;
                n2 = 1;

                for (i = 0; i < m_x; i++) {
                    n1 = n2;
                    n2 = n2 + n2;
                    a = 0;

                    for (j = 0; j < n1; j++) {
                        c = cos_x[a];
                        s = sin_x[a];
                        a += 1 << (m_x - i - 1);

                        for (k = j; k < n_x; k = k + n2) {
                            t1 = c * real[k + n1][indx_col] - s * imag[k + n1][indx_col];
                            t2 = s * real[k + n1][indx_col] + c * imag[k + n1][indx_col];
                            real[k + n1][indx_col] = real[k][indx_col] - t1;
                            imag[k + n1][indx_col] = imag[k][indx_col] - t2;
                            real[k][indx_col] = real[k][indx_col] + t1;
                            imag[k][indx_col] = imag[k][indx_col] + t2;
                        }
                    }
                }

                //  finally, take the conjugate again
                for (i = 0; i < n_x; i++) {
                    imag[i][indx_col] *= -1.;
                }

            }

            // PHASE 2, FFT THE ROWS

            for (int indx_row = 0; indx_row < n_x; indx_row++) {

                //  first, take the conjugate
                for (i = 0; i < n_y; i++) {
                    imag[indx_row][i] *= -1.;
                }

                // Bit-reverse
                j = 0;
                n2 = n_y / 2;
                for (i = 1; i < n_y - 1; i++) {
                    n1 = n2;
                    while (j >= n1) {
                        j = j - n1;
                        n1 = n1 / 2;
                    }
                    j = j + n1;

                    if (i < j) {
                        t1 = real[indx_row][i];
                        real[indx_row][i] = real[indx_row][j];
                        real[indx_row][j] = t1;
                        t1 = imag[indx_row][i];
                        imag[indx_row][i] = imag[indx_row][j];
                        imag[indx_row][j] = t1;
                    }
                }

                // FFT
                n1 = 0;
                n2 = 1;

                for (i = 0; i < m_y; i++) {
                    n1 = n2;
                    n2 = n2 + n2;
                    a = 0;

                    for (j = 0; j < n1; j++) {
                        c = cos_y[a];
                        s = sin_y[a];
                        a += 1 << (m_y - i - 1);

                        for (k = j; k < n_y; k = k + n2) {
                            t1 = c * real[indx_row][k + n1] - s * imag[indx_row][k + n1];
                            t2 = s * real[indx_row][k + n1] + c * imag[indx_row][k + n1];
                            real[indx_row][k + n1] = real[indx_row][k] - t1;
                            imag[indx_row][k + n1] = imag[indx_row][k] - t2;
                            real[indx_row][k] = real[indx_row][k] + t1;
                            imag[indx_row][k] = imag[indx_row][k] + t2;
                        }
                    }
                }

                //  finally, take the conjugate again and scale
                for (i = 0; i < n_y; i++) {
                    real[indx_row][i] *= ifft_scale_factor;
                    imag[indx_row][i] *= -ifft_scale_factor;
                }

            }
        }
    }
