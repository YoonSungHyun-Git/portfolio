#ifndef LEGACY_SIGNAL_H
#define LEGACY_SIGNAL_H

void unwrap_phase(const double *in, int n, double *out);
void moving_average(const double *in, int n, int w, double *out);

#endif
