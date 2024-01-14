KOTLIN_LANGUAGE_LEVEL = "1.9"
JAVA_LANGUAGE_LEVEL = "1.8"

def rules_metadata(name, version, sha, github_project = "", url_template = ""):
    #print("name: %s" % name)
    if github_project == "":
        project = name
        org = "bazelbuild"
    else:
        elements = github_project.split("/")

        # print("elements %s (len=%s)" % (elements, len(elements)))
        if len(elements) == 2:
            project = elements[1]
            org = elements[0]
        else:
            project = elements[0]
            org = "bazelbuild"

    # print("slug: %s/%s" % (org, project))
    template = url_template if url_template != "" else "https://github.com/{o}/{p}/releases/download/{v}/{p}-{v}.tar.gz"

    # print("template: %s" % template)
    url = template.format(o = org, p = project, v = version)

    # print("url: %s" % url)
    return struct(
        name = name,
        version = version,
        project = project,
        sha = sha,
        url = url,
    )

RULES_JAVA_VERSION = "7.3.2"
RULES_JAVA_SHA = "3121a00588b1581bd7c1f9b550599629e5adcc11ba9c65f482bbd5cfe47fdf30"
RULES_JAVA_URL = "https://github.com/bazelbuild/rules_java/releases/download/{v}/rules_java-{v}.tar.gz".format(v = RULES_JAVA_VERSION)

RULES_KOTLIN_VERSION = "1.9.0"
RULES_KOTLIN_SHA = "5766f1e599acf551aa56f49dab9ab9108269b03c557496c54acaf41f98e2b8d6"
RULES_KOTLIN_URL = "https://github.com/bazelbuild/rules_kotlin/releases/download/v{v}/rules_kotlin-v{v}.tar.gz".format(v = RULES_KOTLIN_VERSION)

RULES_JVM_EXTERNAL_TAG = "4.5"
RULES_JVM_EXTERNAL_SHA = "b17d7388feb9bfa7f2fa09031b32707df529f26c91ab9e5d909eb1676badd9a6"
RULES_JVM_EXTERNAL_URL = "https://github.com/bazelbuild/rules_jvm_external/archive/{v}.zip".format(v = RULES_JVM_EXTERNAL_TAG)

RULES_PROTO_VERSION = "5.3.0-21.7"
RULES_PROTO_SHA = "dc3fb206a2cb3441b485eb1e423165b231235a1ea9b031b4433cf7bc1fa460dd"
RULES_PROTO_URL = "https://github.com/bazelbuild/rules_proto/archive/refs/tags/{v}.tar.gz".format(v = RULES_PROTO_VERSION)

IO_GRPC_JAVA_VERSION = "1.58.1"
IO_GRPC_JAVA_SHA = "eb32ad4afe61485638c1baa37dd4408ca5c48a0ae3e5d2c3adc1cdf8fb702993"
IO_GRPC_JAVA_URL = "https://github.com/grpc/grpc-java/archive/v%s.zip" % IO_GRPC_JAVA_VERSION

IO_GRPC_KOTLIN_VERSION = "1.4.1"
IO_GRPC_KOTLIN_SHA = "b576019f9222f47eef42258e5d964c04d87a01532c0df1a40a8f9fa1acc301c8"
IO_GRPC_KOTLIN_URL = "https://github.com/grpc/grpc-kotlin/archive/v%s.zip" % IO_GRPC_KOTLIN_VERSION

COM_GOOGLE_PROTOBUF = rules_metadata(
    name = "com_github_protocolbuffers_protobuf",
    version = "3.25.1",
    sha = "5980276108f948e1ada091475549a8c75dc83c193129aab0e986ceaac3e97131",
    github_project = "protocolbuffers/protobuf",
    url_template = "https://github.com/{o}/{p}/releases/download/v{v}/{p}-all-{v}.zip",
)

RULES_PKG = rules_metadata(
    name = "rules_pkg",
    version = "0.9.1",
    sha = "8f9ee2dc10c1ae514ee599a8b42ed99fa262b757058f65ad3c384289ff70c4b8",
)

BAZEL_SKYLIB = rules_metadata(
    name = "bazel_skylib",
    version = "1.5.0",
    sha = "cd55a062e763b9349921f0f5db8c3933288dc8ba4f76dd9416aac68acee3cb94",
    github_project = "bazel-skylib",
)

RULES_RUBY = rules_metadata(
    name = "rules_ruby",
    version = "0.4.1",
    github_project = "bazel-contrib/rules_ruby",
    sha = "e3495d0129222572654cc5dd5c72c6c997513d65fb8649f43a860ab15334a1c2",
    url_template = "https://github.com/{o}/{p}/releases/download/v{v}/{p}-v{v}.tar.gz",
)
