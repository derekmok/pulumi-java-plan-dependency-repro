import * as random from "@pulumi/random";

const a = new random.RandomString("a", {
    length: 8,
    special: false,
});

const b = new random.RandomPet("b", {
    prefix: a.result,
});

export const aResult = a.result;
export const bId = b.id;
