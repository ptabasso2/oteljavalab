FROM rust:1.76 as builder

WORKDIR /app/
COPY . .
RUN cargo build -r


FROM debian:bookworm-slim as release

WORKDIR /app
COPY --from=builder /app/target/release/dataprocessing /app/dataprocessing

EXPOSE 9000
ENTRYPOINT ["/app/dataprocessing"]
