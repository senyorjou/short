# short

A simple url shortener with almost no dependencies, only redis as a front cache, data stored on a embedded sqlite database


## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for develop the application, run:

``` shell
# run redis backend by ur own or: 
docker-compose up redis
lein ring server  # or server-headless

```

To start the application standalone

``` shell
docker-compose up
```

And visit http://localhost:3000

### Road to profit

- [ ] Tests
- [ ] Decouple dependencies
- [ ] Add parametrization
- [ ] Replace redis by a simple atom and a ring
- [ ] URLs history log
- [ ] HTML stats


## License

Copyright © 2021 Senyor Jou
