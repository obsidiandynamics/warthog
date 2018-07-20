Testing Notes
===
Collection of useful commands for testing with Git.

Deleting a tag on the remote:
```sh
git tag -d 0.3.0 && git push origin :refs/tags/0.3.0
```

Rewinding remote repository to a specific commit:

**Note:** This operation rewrites the remote history; **use with extreme care**.

```sh
git reset --hard 1b5108 && git push --force
```
