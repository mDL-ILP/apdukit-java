test:
	gradle check
ci:
	gitlab-runner exec docker test