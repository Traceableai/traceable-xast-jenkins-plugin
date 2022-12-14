## About
**traceable**: Traceable API Security Testing Enablement

## Installation
Traceable **traceable** is available on PyPI:
```
python3 -m pip install traceable --prefix=./
```

## Config Management
The config dir is by default `$HOME/.traceable/` or can be set by environment variable `TRACEABLE_HOME`.

```
.traceable/
  |   creds.yaml # Will store the creds used for future
  │   config.yaml # All custom configuration needs to go in this.
  │   default.yaml # Default configs and plugins. Downloaded on login to platform from cli. Do not modify.
  └───plugins/
  |   └───custom/ # Custom plugins
  |       └───csrfvalidation.py
  └───hooks/ # Hooks for custom code insertion in plugin logic. Can be used pre/post attack
  |    └───login.py
  |    └───csrf.py
  └───data/ # Data directory of scans
```

- **data:** The fist subdirectory is named with the scan ID, and it contains a file for each test downloaded from the platform
- **plugins:** Each plugin file needs to be in the category supported. A custom plugin sample is provided in the examples folder.
- **hooks:** Hooks are used to minor checks, modifications, refreshing credentials before each test run. They can be scoped for a plugin or applied globally.

## User Configuration File
For more options look for examples folder
config.yaml

**Minimal Config**
Can be generated using the login command
`creds.yaml`
```yaml # creds.yaml
# traceable_server is used to run scans using SaaS or on-prem environments
traceable_server: api.traceable.ai:443
token: <token here>
```

**Adding custom plugins**

Write your own custom plugin refering the examples in the examples folder.
Add the name atrribute of your custom plugin here. All plugin names are preferred to be in lowercase.
`config.yaml`
```yaml # config.yaml
scan:
  plugins:
    custom:
      disabled: false
      csrfvalidation: {}
```
## Usage
**traceable**
`traceable --help`

```
Traceable CLI
Usage: traceable [OPTIONS] COMMAND [ARGS]...

  A CLI tool for Traceable services

Options:
  --help  Show this message and exit.

Commands:
  ast      Application Security Testing commands
  login    Login to Traceable and download TRACEABLE configurations
  version  Show version
```

**traceable login**
`traceable login --help`

```
 Traceable CLI
Usage: traceable login [OPTIONS]

Options:
  --token TEXT                    Token for the scan. It will be picked from
                                  config file or environment if not specified
  -h, --traceable-server TEXT     Traceable Server. It will be picked from
                                  config file or environment if not specified
                                  [default: (api.traceable.ai:443)]
  --clean                         If true, All files in the data directory
                                  will be deleted
  -l, --loglevel [NOTSET|DEBUG|INFO|WARNING|ERROR|CRITICAL|TRACE]
                                  [default: INFO]
  --help                          Show this message and exit.
```

**traceable scan**
`traceable scan --help`

```
Traceable CLI
Usage: traceable ast scan [OPTIONS] COMMAND [ARGS]...

  Control the Traceable AST scans execution and lifecycle

Options:
  --help  Show this message and exit.

Commands:
  abort              Abort the current scan
  init               Initialize a new scan
  initAndRun         Initialize a new scan and run it
  list               List active scans
  report             Get the scan report
  report-defectdojo  Get the scan summary report for DefectDojo
  run                Run the current initialized scan
  stop               Stop the current scan
```

**traceable scan initAndRun**
```
Traceable CLI
Usage: traceable ast scan initAndRun [OPTIONS]

Options:
  -n, --scan-name TEXT            Name of the scan  [default:
                                  (scan_2022-10-31T13:10:03)]
  -st, --scan-tag TEXT            Tags for the scan, Eg: buildId=123
  -e, --traffic-env TEXT          Traffic environment to use  [required]
  -r, --reference-env TEXT        Reference environment to use learnings from
  -i, --include-url-regex TEXT    Include urls matching this regex  [default:
                                  (.*)]
  -x, --exclude-url-regex TEXT    Exclude urls matching this regex  [default:
                                  (.*logout.*)]
  -u, --target-url TEXT           Override URL protocol, host and port. Path
                                  and query string are ignored.
  -l, --loglevel [NOTSET|DEBUG|INFO|WARNING|ERROR|CRITICAL|TRACE]
                                  [default: INFO]
  -bi, --build-id TEXT            Build ID
  -bu, --build-url TEXT           Build URL
  --exclude-plugins TEXT          Exclude the specified plugins, list
                                  available plugins using "traceable plugins"
  --plugins TEXT                  Only run the specified plugins, list
                                  available plugins using "traceable plugins"
  --max-retries INTEGER           Max retries on connection failure for tests
                                  [default: 1]
  --idle-timeout INTEGER          Idle timeout for the scan in minutes
                                  [default: 10]
  --scan-timeout INTEGER          Scan timeout for the platform in minutes
                                  [default: 120]
  --token TEXT                    Token for the scan. It will be picked from
                                  config file or environment if not specified
  --logfile TEXT                  File name to save the logs, by default
                                  TRACEABLE outputs to stdout
  -h, --traceable-server TEXT     Traceable Server. It will be picked from
                                  config file or environment if not specified
                                  [default: (api.traceable.ai:443)]
  -f, --output-format [text|json|csv]
                                  Output format for the results.  [default:
                                  text]
  --request-delay INTEGER         Delay between HTTP requests in milliseconds
                                  [default: 0]
  --threads INTEGER               Number of concurrent threads to use
                                  [default: 16]
  --help                          Show this message and exit.
```


### Examples

**Login to traceable**
`traceable login --token <TOKEN>`
Once you login, cli will download all plugin configurations.

```
Traceable CLI
[2022-10-31 13:11:29,451]:[MainThread]: INFO: Setting log level to: INFO
[2022-10-31 13:11:31,449]:[MainThread]: INFO: Connection ok. Configurations downloaded.
[2022-10-31 13:11:31,506]:[MainThread]: INFO: Configurations stored.
[2022-10-31 13:11:31,506]:[MainThread]: INFO: Initializing config from /home/ubuntu/.traceable
[2022-10-31 13:11:31,516]:[MainThread]: INFO: Default config loaded from /home/ubuntu/.traceable/default.yaml
...
```

**Run the scan.**

`traceable scan initAndRun --traffic-env "sectest" --exclude-url-regex "signup|8025" `

```
traceable scan initAndRun --traffic-env "sectest" --exclude-url-regex "signup|8025"
Traceable CLI
[2022-10-31 13:13:15,449]:[MainThread]: INFO: Setting log level to: INFO
[2022-10-31 13:13:15,449]:[MainThread]: INFO: Refreshing config
[2022-10-31 13:13:16,344]:[MainThread]: INFO: Loading hooks from {'/home/ubuntu/.traceable/hooks'}
[2022-10-31 13:13:16,345]:[MainThread]: INFO: Loaded 4 hooks definition: remove_csrf_token_posthook, set_csrf_token_prehook
[2022-10-31 13:13:16,346]:[MainThread]: INFO: Loading custom plugins from /home/ubuntu/.traceable/plugins/custom
[2022-10-31 13:13:16,347]:[MainThread]: INFO: Custom plugin found: sample_plugin is a valid custom plugin
[2022-10-31 13:13:16,348]:[MainThread]: INFO: Added custom plugin sample_plugin
[2022-10-31 13:13:16,348]:[MainThread]: INFO: Loaded 1 custom plugins definition: sample_plugin
[2022-10-31 13:13:16,354]:[MainThread]: INFO: Scan init: Scan name: scan_2022-10-31T13:13:15, Scan tags: None, Traffic env: sectest, Reference env: , Target url: , Include url regex: .*, Exclude url regex: signup|8025
[2022-10-31 13:13:16,354]:[MainThread]: INFO: Plugins to be used: ['unauthenticated_access', 'bola', 'mass_assignment', ...
[2022-10-31 13:13:17,225]:[MainThread]: INFO: Scan initialized with ID: 66911e8b-b364-4f45-b3d1-8dc3f544fde5, Name: scan_2022-10-31T13:13:15 and status: initialized
[2022-10-31 13:13:17,232]:[MainThread]: INFO: Scan running: Using remote tests
[2022-10-31 13:15:31,455]:[MainThread]: INFO: Running scan with ID e47fed76-6ff8-4f76-8467-5154da4d5baf
[2022-10-31 13:13:17,451]:[MainThread]: INFO: Preparing to receive test suites...
...
```

Now send some traffic to your instrumented traceable application on the traffic-env and traceable will take care of finding vulnerabilities for you.
