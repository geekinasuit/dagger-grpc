load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load(
    ":versions.bzl",
    "BAZEL_SKYLIB",
    "COM_GOOGLE_PROTOBUF",
    "IO_GRPC_JAVA_SHA",
    "IO_GRPC_JAVA_URL",
    "IO_GRPC_JAVA_VERSION",
    "IO_GRPC_KOTLIN_SHA",
    "IO_GRPC_KOTLIN_URL",
    "IO_GRPC_KOTLIN_VERSION",
    "RULES_JAVA_SHA",
    "RULES_JAVA_URL",
    "RULES_JVM_EXTERNAL_SHA",
    "RULES_JVM_EXTERNAL_TAG",
    "RULES_JVM_EXTERNAL_URL",
    "RULES_KOTLIN_SHA",
    "RULES_KOTLIN_URL",
    "RULES_PKG",
    "RULES_PROTO_SHA",
    "RULES_PROTO_URL",
    "RULES_PROTO_VERSION",
)

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = RULES_JVM_EXTERNAL_URL,
)

http_archive(
    name = "rules_java",
    sha256 = RULES_JAVA_SHA,
    urls = [RULES_JAVA_URL],
)

http_archive(
    name = "io_bazel_rules_kotlin",
    sha256 = RULES_KOTLIN_SHA,
    urls = [RULES_KOTLIN_URL],
)

http_archive(
    name = "rules_proto",
    sha256 = RULES_PROTO_SHA,
    strip_prefix = "rules_proto-{v}".format(v = RULES_PROTO_VERSION),
    url = RULES_PROTO_URL,
)

http_archive(
    name = "io_grpc_grpc_java",
    sha256 = IO_GRPC_JAVA_SHA,
    strip_prefix = "grpc-java-%s" % IO_GRPC_JAVA_VERSION,
    url = IO_GRPC_JAVA_URL,
)

http_archive(
    name = "io_grpc_grpc_kotlin",
    sha256 = IO_GRPC_KOTLIN_SHA,
    strip_prefix = "grpc-kotlin-%s" % IO_GRPC_KOTLIN_VERSION,
    url = IO_GRPC_KOTLIN_URL,
)

http_archive(
    name = RULES_PKG.name,
    sha256 = RULES_PKG.sha,
    urls = [RULES_PKG.url],
)

http_archive(
    name = BAZEL_SKYLIB.name,
    sha256 = BAZEL_SKYLIB.sha,
    urls = [BAZEL_SKYLIB.url],
)

http_archive(
    name = COM_GOOGLE_PROTOBUF.name,
    sha256 = COM_GOOGLE_PROTOBUF.sha,
    strip_prefix = "protobuf-%s" % COM_GOOGLE_PROTOBUF.version,
    urls = [COM_GOOGLE_PROTOBUF.url],
)

load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")

bazel_skylib_workspace()

load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")

rules_pkg_dependencies()

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

load("@rules_java//java:repositories.bzl", "rules_java_dependencies", "rules_java_toolchains")

rules_java_dependencies()

rules_java_toolchains()

load(":dependencies.bzl", "load_maven_artifacts")

load_maven_artifacts()

load("@maven//:compat.bzl", "compat_repositories")

compat_repositories()

load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories")

kotlin_repositories()  # if you want the default. Otherwise see custom kotlinc distribution below

load("@io_bazel_rules_kotlin//kotlin:core.bzl", "kt_register_toolchains")

kt_register_toolchains()  # to use the default toolchain, otherwise see toolchains below

load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")

grpc_java_repositories()

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()
