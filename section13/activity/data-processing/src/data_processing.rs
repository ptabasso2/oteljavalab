use actix_web::{get, Responder};
use log::{info, warn};
use std::env;
use std::io;

#[get("/processTemperature")]
pub async fn process_temp() -> impl Responder {
    if do_processing().await {
        let resp = get_temperature().await.unwrap();
        return format!("{resp}");
    }

    warn!("Error processing temperature");
    format!("Temperature error")
}

async fn do_processing() -> bool {
    info!("Processing request");
    tokio::time::sleep(tokio::time::Duration::from_millis(1)).await;
    true
}

async fn get_temperature() -> io::Result<String> {
    let client = awc::Client::new();
    let temp_calculator_addr = format!(
        "{}{}",
        env::var("TEMP_CALCULATOR_ADDR").unwrap(),
        "/measureTemperature"
    );

    info!("Requesting temperature from: {}", temp_calculator_addr);

    let mut response = client
        .get(temp_calculator_addr)
        .send()
        .await
        .map_err(|err| io::Error::new(io::ErrorKind::Other, err.to_string()))?;

    let bytes = response
        .body()
        .await
        .map_err(|err| io::Error::new(io::ErrorKind::Other, err.to_string()))?;

    std::str::from_utf8(&bytes)
        .map(|s| s.to_owned())
        .map_err(|err| io::Error::new(io::ErrorKind::Other, err))
}
