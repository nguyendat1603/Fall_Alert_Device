# A synthetic multirate time-series dataset for benchmarking health monitoring and wearable fall detection in older adults

Dataset release: 3.2.0. Generator: 3.1.0. Author: M. Sabrigiriraj, Hindusthan College of Engineering and Technology, Coimbatore, Tamil Nadu, India.

## Contents
This repository ZIP contains exactly 43 files: 34 original notebook-generated outputs, one byte-preserved executed notebook, and eight newly prepared repository documentation/verification files. All files are at the archive root.

Main data:
- synthetic_subjects.csv: 120 rows, 21 columns.
- synthetic_event_metadata.csv: 960 rows, 22 columns.
- synthetic_elderly_monitoring_timeseries.parquet: 362240 rows, 45 columns.
- event_features.csv: 960 rows, 107 columns.
- internal_heldout_predictions.csv: 576 rows, 12 columns.
- data_dictionary.csv: 207 file-specific column entries across these five tables.

Subjects are aged 65-95, with eight four-second events each. Subject splits are 84/18/18 and event splits 672/144/144 for training/validation/test. Sampling rates are 20, 100 and 200 Hz. There are 124 fall events and 337 hard-negative daily-activity events. All generated primary records are synthetic.

## Interpretation
Use for controlled benchmarking, teaching and stress testing. This is not a clinical dataset, a population prevalence estimate or a validated diagnostic resource. The health-risk label is a simulation target defined by thresholding latent risk probability at 0.30; the separate Bernoulli outcome is not the benchmark target. Fall models use inertial features; health-risk models use physiological summaries and demographic/comorbidity variables only.

Internal test fall F1 is 0.956522 for both models. SisFall recording-level F1 is 0.778295/0.661355 and older-labelled ADL false-positive rates are 0.370824/0.101336 (logistic regression/random forest). These are transfer stress-test results, not clinical validation. Several sensor-profile test groups have only one or two subjects.

## Reproduction and provenance
Read REPRODUCIBILITY.md. environment_versions.csv records Python 3.13.15, NumPy 2.1.3 and pandas 2.2.3, plus other packages. The notebook metadata retains 3.2.2-single-sisfall-input; its executed code and manifest use 3.2.2-documentation-and-packaging-repair. The supplied notebook has 49 cells, including 37 code cells with execution counts 1-37 and no saved error outputs.

Parent calibration source: https://doi.org/10.17632/tf56mbyg69.2 (itself simulated).
Parent article: https://doi.org/10.1016/j.dib.2026.112641.
Audit letter: https://doi.org/10.1016/j.dib.2026.113001.
SisFall: https://doi.org/10.3390/s17010198.
Neither the parent CSV nor the SisFall source archive is redistributed.

## Verification and citation
repository_file_inventory.csv and repository_checksums_sha256.csv cover 41 payload files. These two inventory files exclude themselves to avoid circular checksums. The external archive checksum covers the entire ZIP. PACKAGING_VALIDATION.json records the packaging audit scope. CITATION.cff provides a citation without an invented repository DOI; use the DOI assigned by Mendeley after deposit.

Licence: CC BY 4.0; see LICENSE.txt. No separate software licence is asserted for the supplied notebook.
