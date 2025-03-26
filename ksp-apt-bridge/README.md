# APT-KSP Bridge

This library implements a partial Kotlin Symbol Processing API built on top of
Java Annotation Processing types. This permits parts of a KSP processor to be
used inside of a Java Annotation Processor. It does not fully implement KSP API,
and is not intended for general use, mostly just for use by Dagger-GRPC, though
it may find utility elsewhere.

### Why not Room's XProcessing system?

androidx.room has a XProcessing library which some code-generators in the kotlin and
java space use to allow for a common architecture for generating code from annotations.
It is a far more featureful environment than this bridge, which doesn't attempt to
capture the semantics of both in one processing system. Rather, it merely attempts to
provide convenience implementations of KSP-API, really only a fraction of it. It is
intended to be vastly more light-weight. 