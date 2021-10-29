# InferRules

For given input before-after code snippets across an edit, `InferRules` produces a rewrite rule for it.

To build :
`./gradlew build`


#### Running as a binary

Download the latest release, or run `./gradlew distZip` and go to  `/distributions/`.

Unzip `InferRules-1.0-SNAPSHOT.zip`
Got to `/bin`
Run `./Infer -b "<Before Code Snippet>" -a "<After Code Snippet>" -l "<Languag>"`
  
Example: 
` ./InferRules -b "count = 0\nfor e in es:\n    count += e\nprint(count)" -a "count = np.sum(es)" -l "Python"`
