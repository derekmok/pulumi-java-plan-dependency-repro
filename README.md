# pulumi-java saved-plan dependency repro

This is a small reproduction of a Pulumi Java SDK bug that needs no cloud credentials. Resource B takes an output of resource A, and A is created in the same update. In that case, `pulumi preview --save-plan` records no dependency on A. `pulumi up --plan` then creates A and fails on B:

```
violates plan: dependencies changed: added urn:...::RandomString::a
```

The same program in TypeScript works.

## Environment

| Component | Version |
|---|---|
| Pulumi CLI | 3.263.0 |
| Java | Temurin OpenJDK 25.0.1, Gradle 9.6.0 (wrapper) |
| Java SDKs | `com.pulumi:pulumi:1.36.3`, `com.pulumi:random:4.21.2` |
| Node | v24.13.0 |
| Node SDKs | `@pulumi/pulumi@3.263.0`, `@pulumi/random@4.21.2` |
| OS | macOS 27.0 (darwin arm64) |

## Program

The Java and TypeScript programs are equivalent:

```java
var a = new RandomString("a", RandomStringArgs.builder().length(8).special(false).build());
var b = new RandomPet("b", RandomPetArgs.builder().prefix(a.result()).build());
```

## Steps

This uses a local file backend, so you don't need to log in to Pulumi Cloud.

```bash
export PULUMI_BACKEND_URL="file://$PWD/.pulumi-state"
export PULUMI_CONFIG_PASSPHRASE=repro-passphrase
mkdir -p .pulumi-state

cd java            # or: cd typescript && npm install
pulumi stack init dev
pulumi preview --save-plan plan.json
pulumi up --plan plan.json --yes --skip-preview
```

## Expected vs actual

**Expected:** the saved plan records A as a dependency of B, and `pulumi up --plan` succeeds.

**Actual:** in Java, the saved plan has no dependency on A, and `pulumi up --plan` fails on B:

```
random:index:RandomPet b  error: resource urn:pulumi:dev::plan-deps-java::random:index/randomPet:RandomPet::b violates plan: dependencies changed: added urn:pulumi:dev::plan-deps-java::random:index/randomString:RandomString::a
```

In TypeScript, `pulumi up --plan` succeeds.

Saved plan for Java (`resourcePlans[B].goal`):

```json
{
  "dependencies": null,
  "propertyDependencies": { "prefix": [] }
}
```

Saved plan for TypeScript:

```json
{
  "dependencies": ["urn:pulumi:dev::plan-deps-ts::random:index/randomString:RandomString::a"],
  "propertyDependencies": {
    "prefix": ["urn:pulumi:dev::plan-deps-ts::random:index/randomString:RandomString::a"]
  }
}
```
