Simple Tuner
============

This is a simple, ad-free instrument tuning Android app.

Here's a brief overview of how it works:

1. Compute the [autocorrelation](https://en.wikipedia.org/wiki/Autocorrelation) of the audio signal.
   This *should* give a periodic plot with peaks where the signal perfectly overlaps with itself.
2. Determine the period by computing the zero crossings.  The actual implementation is via a grid
   search over some offset values.
3. Compute the mean and standard deviation of the observed period and update a
   [Kalman filter](https://en.wikipedia.org/wiki/Kalman_filter) that gives a smoothed estimate of
   the period.
4. Find the closest musical pitch in the
   [12-tone equal temperament](https://en.wikipedia.org/wiki/12_equal_temperament) system and
   compute the error in frequency and [cents](https://en.wikipedia.org/wiki/Cent_(music)).
5. Update the frame of the tuner graphic as a function of the error in cents.
   

Privacy Policy
==============

The Simple Tuner app requires the use of your device's microphone to record audio.  This audio data
is not persisted in any permanent storage and is processed on your device.  No other personally
identifiable information is used by the Simple Tuner app.


License
=======

This project is covered by the GNU General Public License v3.  See the 
[LICENSE file](https://github.com/ankwok/strobe-tuner/blob/master/LICENSE).