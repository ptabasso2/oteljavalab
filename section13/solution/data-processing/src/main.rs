use actix_web::{App, HttpServer};
use actix_web_opentelemetry::RequestTracing;

mod data_processing;
use data_processing::process_temp;

use log::LevelFilter;
use simplelog::{Config, SimpleLogger};

mod telemetry_conf;
use telemetry_conf::init_tracer;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    let _ = SimpleLogger::init(LevelFilter::Info, Config::default());
    init_tracer();

    HttpServer::new(|| App::new().wrap(RequestTracing::new()).service(process_temp))
        .bind(("0.0.0.0", 9000))?
        .run()
        .await
}
