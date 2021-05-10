module.exports = {
  root: true,
  env: {
    es6: true,
    node: true,
  },
  extends: [
    "eslint:recommended",
    "google",
  ],
  rules: {
    quotes: ["error", "double"],
  },
};


module.exports ={
  rules: {
    'max-len': ["error", { "code": 120 }]
  }
}
