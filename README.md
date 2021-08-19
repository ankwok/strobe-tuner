Strobe Tuner
============

This is a simple [strobe tuner](https://en.wikipedia.org/wiki/Electronic_tuner#Strobe_tuners)
Android app.  Of course, actual strobe tuners use spinning disks and a strobe light to achieve the
illusion of moving bars.  This app is a digital imitation of that effect.  As this is my first
serious Android app (and the fact that I'm not a UX type of guy), this app emphasizes accuracy and
simplicity over fancy graphics and effects.

Here's a brief overview of how it works:

1. Compute the [autocorrelation](https://en.wikipedia.org/wiki/Autocorrelation) of the audio signal.
   This *should* give a periodic plot with peaks where the signal perfectly overlaps with itself.
1. Determine the period by computing the zero crossings.  The actual implementation is via a grid
   search over some offset values.
1. Compute the mean and standard deviation of the observed period and update a
   [Kalman filter](https://en.wikipedia.org/wiki/Kalman_filter) that gives a smoothed estimate of
   the period.
1. Find the closest musical pitch in the
   [12-tone equal temperament](https://en.wikipedia.org/wiki/12_equal_temperament) system and
   compute the error in frequency and [cents](https://en.wikipedia.org/wiki/Cent_(music)).
1. Update the frame of the strobe graphic as a function of the elapsed time from the previous
   refresh and the error in cents.