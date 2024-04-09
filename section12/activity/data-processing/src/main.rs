use actix_web::{App, HttpServer};
use actix_web_opentelemetry::RequestTracing;

mod data_processing;
use data_processing::process_temp;

mod telemetry_conf;
use telemetry_conf::init_tracer;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    let _ = init_tracer().unwrap();

    HttpServer::new(|| App::new().wrap(RequestTracing::new()).service(process_temp))
        .bind(("0.0.0.0", 9000))?
        .run()
        .await
}
