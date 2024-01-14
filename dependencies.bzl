load("@rules_jvm_external//:defs.bzl", "maven_install")
load(
    "@io_grpc_grpc_java//:repositories.bzl",
    "IO_GRPC_GRPC_JAVA_ARTIFACTS",
    "IO_GRPC_GRPC_JAVA_OVERRIDE_TARGETS",
)

artifacts = [
    "junit:junit:4.13.2",
    "org.hamcrest:hamcrest-library:1.3",
    "com.linecorp.armeria:armeria:1.26.4",
    "com.linecorp.armeria:armeria-grpc:1.26.4",
    "ch.qos.logback:logback-classic:1.4.11",
    "io.github.oshai:kotlin-logging-jvm:5.1.0",
    "io.grpc:grpc-kotlin-stub:1.4.1",
    "com.google.code.findbugs:jsr305:3.0.2",
] + IO_GRPC_GRPC_JAVA_ARTIFACTS

override_targets = {
    #         "your.target:artifact": "@//third_party/artifact",
} | IO_GRPC_GRPC_JAVA_OVERRIDE_TARGETS

def load_maven_artifacts():
    maven_install(
        artifacts = artifacts,
        repositories = [
            "m2local",
            "https://maven.google.com",
            "https://repo1.maven.org/maven2",
        ],
        override_targets = override_targets,
        generate_compat_repositories = True,
        fetch_sources = True,
    )
